package com.tonapps.tonkeeper.ui.screen.battery.refill

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.billing.BillingManager
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
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.TokenRepository
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import uikit.extensions.collectFlow
import java.net.URLEncoder

class BatteryRefillViewModel(
    app: Application,
    private val api: API,
    private val accountRepository: AccountRepository,
    private val batteryRepository: BatteryRepository,
    private val tokenRepository: TokenRepository,
    private val settingsRepository: SettingsRepository,
    private val billingManager: BillingManager,
) : BaseWalletVM(app) {

    private val promoStateFlow = MutableStateFlow<PromoState>(PromoState.Default)

    val uiItemsFlow = combine(
        accountRepository.selectedWalletFlow,
        promoStateFlow,
        settingsRepository.walletPrefsChangedFlow,
        batteryRepository.balanceUpdatedFlow,
    ) { wallet, promoState, _, _ ->
        val batteryBalance = getBatteryBalance(wallet)

        val uiItems = mutableListOf<Item>()
        uiItems.add(uiItemBattery(batteryBalance, api.config))
        uiItems.add(Item.Space)

        if (!api.config.batteryPromoDisabled) {
            uiItems.add(Item.Promo(promoState))
            uiItems.add(Item.Space)
        }

        if (batteryBalance.balance.isPositive) {
            uiItems.add(Item.Settings(settingsRepository.getBatteryTxEnabled(wallet.accountId)))
            uiItems.add(Item.Space)
        }

        if (!api.config.disableBatteryCryptoRecharge) {
            uiItems.addAll(uiItemsRechargeMethods(wallet))
        }

        val tonProofToken = accountRepository.requestTonProofToken(wallet) ?: ""

        uiItems.add(Item.Space)
        uiItems.add(
            Item.Refund(
                refundUrl = "${api.config.batteryRefundEndpoint}?token=${
                    URLEncoder.encode(
                        tonProofToken,
                        "UTF-8"
                    )
                }&testnet=${wallet.testnet}"
            )
        )

        uiItems.toList()
    }.flowOn(Dispatchers.IO)

    init {
        accountRepository.selectedWalletFlow.take(1).map { wallet ->
            val appliedPromo = batteryRepository.getAppliedPromo(wallet.testnet)

            if (appliedPromo.isNullOrBlank()) {
                promoStateFlow.tryEmit(PromoState.Default)
            } else {
                promoStateFlow.tryEmit(PromoState.Applied(appliedPromo))
            }
        }.flowOn(Dispatchers.IO).launchIn(viewModelScope)
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
            uiItems.add(
                Item.RechargeMethod(
                    position = position,
                    token = supportToken
                )
            )
        }
        uiItems.add(Item.Gift(position = ListCell.Position.LAST))
        return uiItems.toList()
    }

    private suspend fun getBatteryConfig(
        wallet: WalletEntity
    ): BatteryConfigEntity {
        return batteryRepository.getConfig(wallet.testnet)
    }

    private suspend fun getBatteryBalance(
        wallet: WalletEntity
    ): BatteryBalanceEntity {
        val tonProofToken =
            accountRepository.requestTonProofToken(wallet) ?: return BatteryBalanceEntity.Empty
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
            supportTokenAddress.contains(token.address)
        }.sortedBy { it.fiat }.reversed()
    }

    fun applyPromo(promo: String) = accountRepository.selectedWalletFlow.take(1).map { wallet ->
        if (promo.isEmpty()) {
            promoStateFlow.tryEmit(PromoState.Default)
            return@map
        }
        promoStateFlow.tryEmit(PromoState.Loading)
        try {
            api.battery(wallet.testnet).verifyPurchasePromo(promo)
            batteryRepository.setAppliedPromo(wallet.testnet, promo)
            promoStateFlow.tryEmit(PromoState.Applied(promo))
        } catch (_: Exception) {
            promoStateFlow.tryEmit(PromoState.Error)
        }
    }.flowOn(Dispatchers.IO).launchIn(viewModelScope)
}