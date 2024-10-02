package com.tonapps.tonkeeper.ui.screen.battery.refill

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.Purchase
import com.tonapps.extensions.filterList
import com.tonapps.extensions.toBigDecimalSafe
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.Environment
import com.tonapps.tonkeeper.billing.BillingManager
import com.tonapps.tonkeeper.billing.ProductEntity
import com.tonapps.tonkeeper.billing.isSuccess
import com.tonapps.tonkeeper.billing.walletId
import com.tonapps.tonkeeper.extensions.loading
import com.tonapps.tonkeeper.extensions.result
import com.tonapps.tonkeeper.extensions.showToast
import com.tonapps.tonkeeper.ui.base.BaseWalletVM
import com.tonapps.tonkeeper.ui.screen.battery.refill.entity.PromoState
import com.tonapps.tonkeeper.ui.screen.battery.refill.list.Item
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.ConfigEntity
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.AccountRepository
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.battery.BatteryMapper
import com.tonapps.wallet.data.battery.BatteryRepository
import com.tonapps.wallet.data.battery.entity.BatteryBalanceEntity
import com.tonapps.wallet.data.battery.entity.BatteryConfigEntity
import com.tonapps.wallet.data.battery.entity.RechargeMethodEntity
import com.tonapps.wallet.data.battery.entity.RechargeMethodType
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.settings.BatteryTransaction
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.TokenRepository
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class BatteryRefillViewModel(
    app: Application,
    private val wallet: WalletEntity,
    private val api: API,
    private val accountRepository: AccountRepository,
    private val batteryRepository: BatteryRepository,
    private val tokenRepository: TokenRepository,
    private val ratesRepository: RatesRepository,
    private val settingsRepository: SettingsRepository,
    private val billingManager: BillingManager,
    private val environment: Environment,
) : BaseWalletVM(app) {

    private val productsFlow = api.configFlow.map { it.iapPackages }.map { iapPackages ->
        if (!environment.isGooglePlayBillingAvailable) {
            emptyList()
        } else {
            billingManager.getProducts(iapPackages)
        }
    }.flowOn(Dispatchers.IO)

    private val promoStateFlow = MutableStateFlow<PromoState>(PromoState.Default)

    private val triggerFlow = combine(
        settingsRepository.walletPrefsChangedFlow,
        batteryRepository.balanceUpdatedFlow
    ) { _, _ -> }

    val uiItemsFlow = combine(
        promoStateFlow,
        productsFlow,
        triggerFlow,
        api.configFlow,
    ) { promoState, products, _, apiConfig ->
        val tonProof = getTonProof()
        val batteryBalance = getBatteryBalance(tonProof)

        val uiItems = mutableListOf<Item>()
        uiItems.add(uiItemBattery(batteryBalance, apiConfig))
        uiItems.add(Item.Space)

        if (!apiConfig.batteryPromoDisabled) {
            uiItems.add(Item.Promo(promoState))
            uiItems.add(Item.Space)
        }

        if (batteryBalance.balance.isPositive) {
            uiItems.add(Item.Settings(settingsRepository.getBatteryTxEnabled(wallet.accountId)))
            uiItems.add(Item.Space)
        }

        if (environment.isGooglePlayBillingAvailable && !apiConfig.disableBatteryIapModule) {
            uiItems.addAll(uiItemsPackages(
                batteryBalance = batteryBalance,
                config = apiConfig,
                products = products
            ))
            uiItems.add(Item.Space)
        }

        val rechargeMethodsItems = uiItemsRechargeMethods(wallet)

        if (!apiConfig.disableBatteryCryptoRecharge && rechargeMethodsItems.isNotEmpty()) {
            uiItems.addAll(uiItemsRechargeMethods(wallet))
            uiItems.add(Item.Space)
        }

        val refundUrl = apiConfig.getBatteryRefundUrl(
            token = tonProof,
            testnet = wallet.testnet
        )

        uiItems.add(Item.Refund(
            wallet = wallet,
            refundUrl = refundUrl
        ))

        uiItems.add(Item.Space)
        uiItems.add(Item.RestoreIAP)

        uiItems.toList()
    }.flowOn(Dispatchers.IO)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val appliedPromo = getAppliedPromo()

            if (appliedPromo.isNullOrBlank()) {
                promoStateFlow.tryEmit(PromoState.Default)
            } else {
                promoStateFlow.tryEmit(PromoState.Applied(appliedPromo))
            }
        }

        billingManager.purchasesFlow.filterList { wallet.id == it.walletId }.map { purchases ->
            val tonProof = getTonProof()
            for (purchase in purchases) {
                submitPurchase(tonProof, purchase)
            }
            refreshBatteryBalance(tonProof)
        }.flowOn(Dispatchers.IO).catch {
            context.result(true)
        }.launchIn(viewModelScope)
    }

    private suspend fun getAppliedPromo(): String? {
        return batteryRepository.getAppliedPromo(wallet.testnet)
    }

    private suspend fun uiItemsPackages(
        batteryBalance: BatteryBalanceEntity,
        config: ConfigEntity,
        products: List<ProductEntity>
    ): List<Item.IAPPack> {
        val tonPriceInUsd = ratesRepository.getTONRates(WalletCurrency.USD).getRate(TokenEntity.TON.address)

        val isBatteryEmpty = batteryBalance.reservedBalance.isZero && batteryBalance.balance.isZero
        val reservedAmount = if (isBatteryEmpty) {
            config.batteryReservedAmount.toBigDecimalSafe()
        } else {
            BigDecimal.ZERO
        }

        val uiItems = mutableListOf<Item.IAPPack>()
        for ((index, product) in products.withIndex()) {
            val position = ListCell.getPosition(products.size, index)
            val charges = BatteryMapper.calculateIapCharges(
                userProceed = product.userProceed,
                tonPriceInUsd = tonPriceInUsd,
                reservedAmount = reservedAmount,
                meanFees = config.batteryMeanFees.toBigDecimalSafe()
            )

            val transactions = mapOf(
                BatteryTransaction.SWAP to charges / BatteryMapper.calculateChargesAmount(
                    config.batteryMeanPriceSwap,
                    config.batteryMeanFees
                ),
                BatteryTransaction.NFT to charges / BatteryMapper.calculateChargesAmount(
                    config.batteryMeanPriceNft,
                    config.batteryMeanFees
                ),
                BatteryTransaction.JETTON to charges / BatteryMapper.calculateChargesAmount(
                    config.batteryMeanPriceJetton,
                    config.batteryMeanFees
                )
            )

            uiItems.add(Item.IAPPack(
                position = position,
                product = product,
                charges = charges,
                transactions = transactions,
            ))
        }

        return uiItems.toList()
    }

    private fun uiItemBattery(
        balance: BatteryBalanceEntity,
        config: ConfigEntity,
    ): Item.Battery {
        val charges = BatteryMapper.convertToCharges(balance.balance, api.config.batteryMeanFees)
        val formattedChanges = CurrencyFormatter.format(value = charges.toBigDecimal())

        return Item.Battery(
            balance = balance.balance.value.toFloat(),
            beta = config.batteryBeta,
            changes = charges,
            formattedChanges = formattedChanges
        )
    }

    private suspend fun uiItemsRechargeMethods(
        wallet: WalletEntity,
    ): List<Item> {
        val batteryConfig = getBatteryConfig(wallet)
        val supportedTokens = getSupportedTokens(wallet, batteryConfig.rechargeMethods)

        val uiItems = mutableListOf<Item>()
        for ((index, supportToken) in supportedTokens.withIndex()) {
            val position = ListCell.getPosition(supportedTokens.size + 1, index)
            uiItems.add(Item.RechargeMethod(
                wallet = wallet,
                position = position,
                token = supportToken
            ))
        }
        if (uiItems.isNotEmpty()) {
            uiItems.add(Item.Gift(wallet, position = ListCell.Position.LAST))
        }
        return uiItems.toList()
    }

    private suspend fun getBatteryConfig(
        wallet: WalletEntity
    ): BatteryConfigEntity {
        return batteryRepository.getConfig(wallet.testnet)
    }

    private suspend fun getBatteryBalance(tonProofToken: String): BatteryBalanceEntity {
        return batteryRepository.getBalance(
            tonProofToken = tonProofToken,
            publicKey = wallet.publicKey,
            testnet = wallet.testnet
        )
    }

    private suspend fun getTokens(wallet: WalletEntity): List<AccountTokenEntity> {
        return tokenRepository.get(
            currency = settingsRepository.currency,
            accountId = wallet.accountId,
            testnet = wallet.testnet
        ) ?: emptyList()
    }

    private suspend fun getSupportedTokens(
        wallet: WalletEntity,
        rechargeMethods: List<RechargeMethodEntity>
    ): List<AccountTokenEntity> {
        val tokens = getTokens(wallet)
        val supportTokenAddress = rechargeMethods.filter { it.supportRecharge }.mapNotNull {
            if (it.type == RechargeMethodType.TON) {
                TokenEntity.TON.address
            } else {
                it.jettonMaster
            }
        }
        return tokens.filter { token ->
            supportTokenAddress.contains(token.address) && token.balance.value.isPositive
        }.sortedWith(compareByDescending<AccountTokenEntity> { token ->
            token.isUsdt // Place USDT at the top
        }.thenBy { token ->
            token.isTon // Place TON at the end
        }.thenByDescending { token ->
            token.fiat // Sort by fiat value
        })
    }

    fun applyPromo(promo: String, isInitial: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            if (promo.isEmpty()) {
                promoStateFlow.tryEmit(PromoState.Default)
                return@launch
            }
            val initialPromo = if (isInitial) promo else null
            promoStateFlow.tryEmit(PromoState.Loading(initialPromo = initialPromo))
            try {
                if (isInitial) {
                    delay(2000)
                }
                api.battery(wallet.testnet).verifyPurchasePromo(promo)
                batteryRepository.setAppliedPromo(wallet.testnet, promo)
                promoStateFlow.tryEmit(PromoState.Applied(promo))
            } catch (_: Exception) {
                promoStateFlow.tryEmit(PromoState.Error)
            }
        }
    }

    fun makePurchase(activity: Activity, product: ProductEntity) {
        viewModelScope.launch {
            val result = billingManager.requestPurchase(activity, wallet, product.details)
            if (!result.isSuccess) {
                context.result(false)
            }
        }
    }

    fun restorePurchases() {
        context.loading(true)
        viewModelScope.launch(Dispatchers.Main) {
            try {
                val tonProof = getTonProof()
                val records = billingManager.restorePurchases()
                for (record in records) {
                    val productId = record.products.firstOrNull() ?: continue
                    submitPurchase(tonProof, productId, record.purchaseToken)
                }
                refreshBatteryBalance(tonProof)
                context.result()
            } catch (e: Throwable) {
                context.result(true)
            }
        }
    }

    private suspend fun submitPurchase(
        tonProofToken: String,
        purchase: Purchase
    ) = submitPurchase(
        tonProofToken = tonProofToken,
        productId = purchase.products.first(),
        purchaseToken = purchase.purchaseToken
    )

    private suspend fun submitPurchase(
        tonProofToken: String,
        productId: String,
        purchaseToken: String,
    ) = withContext(Dispatchers.IO) {
        api.batteryPurchase(
            tonProofToken = tonProofToken,
            testnet = wallet.testnet,
            productId = productId,
            purchaseToken = purchaseToken,
            promo = getAppliedPromo()
        )
        billingManager.consumeProduct(purchaseToken)
    }

    private suspend fun refreshBatteryBalance(tonProofToken: String) {
        batteryRepository.getBalance(tonProofToken, wallet.publicKey, wallet.testnet, ignoreCache = true)
    }

    private suspend fun getTonProof(): String {
        return accountRepository.requestTonProofToken(wallet) ?: throw IllegalStateException("proof token is null")
    }
}