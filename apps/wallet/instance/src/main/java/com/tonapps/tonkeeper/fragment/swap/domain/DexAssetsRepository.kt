package com.tonapps.tonkeeper.fragment.swap.domain

import android.net.Uri
import com.tonapps.blockchain.Coin
import com.tonapps.tonkeeper.api.jetton.JettonRepository
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAssetBalance
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAssetRate
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAssetType
import com.tonapps.tonkeeper.fragment.swap.domain.model.SwapSimulation
import com.tonapps.tonkeeper.fragment.swap.domain.model.getRecommendedGasValues
import com.tonapps.wallet.api.StonfiAPI
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.rates.entity.RateEntity
import com.tonapps.wallet.data.rates.entity.RatesEntity
import io.stonfiapi.models.AssetInfoSchema
import io.stonfiapi.models.AssetKindSchema
import io.stonfiapi.models.DexReverseSimulateSwap200Response
import io.tonapi.models.JettonBalance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

class DexAssetsRepository(
    private val api: StonfiAPI,
    private val ratesRepository: RatesRepository,
    private val jettonRepository: JettonRepository
) {

    private val isLoadingFlows = mutableMapOf<String, MutableStateFlow<Boolean>>()

    private val jettonBalancesLock = ReentrantReadWriteLock()
    private val jettonBalancesFlows = mutableMapOf<String, MutableStateFlow<List<JettonBalance>>>()

    private val assetsFlowLock = ReentrantReadWriteLock()
    private val assetsFlows = mutableMapOf<String, MutableStateFlow<List<DexAssetBalance>>>()

    fun getIsLoadingFlow(walletAddress: String): Flow<Boolean> {
        return getIsLoadingMutableFlow(walletAddress)
    }

    fun getAssetsFlow(walletAddress: String): Flow<List<DexAssetBalance>> {
        return getAssetsMutableFlow(walletAddress)
    }

    fun getPositiveBalanceFlow(
        walletAddress: String,
        testnet: Boolean,
        currency: WalletCurrency
    ): Flow<List<DexAssetBalance>> {
        return getJettonBalancesMutableFlow(walletAddress, testnet)
            .map { collectBalances(it, currency) }
            .flowOn(Dispatchers.IO)
    }

    private fun key(walletAddress: String, testnet: Boolean) = "$walletAddress;$testnet"

    private fun getJettonBalancesMutableFlow(
        walletAddress: String,
        testnet: Boolean
    ) = jettonBalancesLock.write {
        val key = key(walletAddress, testnet)
        if (!jettonBalancesFlows.containsKey(key)) {
            jettonBalancesFlows[key] = MutableStateFlow(emptyList())
        }
        jettonBalancesFlows[key]!!
    }

    private fun getIsLoadingMutableFlow(
        walletAddress: String
    ) = assetsFlowLock.write {
        if (!isLoadingFlows.containsKey(walletAddress)) {
            isLoadingFlows[walletAddress] = MutableStateFlow(false)
        }
        isLoadingFlows[walletAddress]!!
    }

    private fun getAssetsMutableFlow(
        walletAddress: String
    ) = assetsFlowLock.write {
        if (!assetsFlows.containsKey(walletAddress)) {
            assetsFlows[walletAddress] = MutableStateFlow(emptyList())
        }
        assetsFlows[walletAddress]!!
    }

    private suspend fun collectBalances(
        jettonBalances: List<JettonBalance>,
        currency: WalletCurrency
    ): List<DexAssetBalance> {
        val tokenAddresses = jettonBalances.map { it.jetton.address }
            .toMutableList()
        val rates = ratesRepository.getRates(currency, tokenAddresses)
        return jettonBalances.map { it.toDomain(rates) }
    }

    suspend fun loadBalances(
        walletAddress: String,
        testnet: Boolean
    ) = withContext(Dispatchers.IO) {
        getJettonBalancesMutableFlow(walletAddress, testnet).apply {
            value = jettonRepository.get(walletAddress, testnet)
                ?.data ?: emptyList()
        }
    }

    private fun JettonBalance.toDomain(
        rates: RatesEntity
    ): DexAssetBalance {
        val entity = TokenEntity(jetton)
        val rate = DexAssetRate(
            tokenEntity = entity,
            currency = rates.currency,
            rate = rates.getRate(entity.address)
        )
        return DexAssetBalance(
            type = type(),
            balance = Coin.toCoins(balance, entity.decimals),
            rate = rate
        )
    }

    private fun JettonBalance.type(): DexAssetType {
        return when  {
            jetton.symbol.contains("wton", ignoreCase = true) -> DexAssetType.WTON
            jetton.symbol.contains("ton", ignoreCase = true) -> DexAssetType.TON
            else -> DexAssetType.JETTON
        }
    }

    suspend fun loadAssets(
        walletAddress: String,
        currency: WalletCurrency
    ) = withContext(Dispatchers.IO) {
        val items = getAssetsMutableFlow(walletAddress)
        val isLoading = getIsLoadingMutableFlow(walletAddress)

        isLoading.value = items.value.isEmpty()
        val response = api.wallets.getWalletAssets(walletAddress)

        val tonToUsdRate = ratesRepository.getRates(WalletCurrency.DEFAULT, "TON")
            .rate("TON")!!
        val tonToCurrencyRate = ratesRepository.getRates(currency, "TON")
            .rate("TON")!!

        items.value = response.assetList
            .asSequence()
            .filter { it.isValid() }
            .map { it.toBalance(tonToUsdRate, tonToCurrencyRate) }
            .sortedWith(dexAssetBalanceComparator)
            .toList()

        isLoading.value = false
    }

    fun getDefaultAsset(walletAddress: String): DexAssetBalance {
        val items = getAssetsMutableFlow(walletAddress)
        return items.value.first { it.type == DexAssetType.TON }
    }

    private val dexAssetBalanceComparator = Comparator<DexAssetBalance> { o1, o2 ->
        val f1 = o1.balance * o1.rate.rate
        val f2 = o2.balance * o2.rate.rate
        f2.compareTo(f1)
            .takeUnless { it == 0 }
            ?: dexAssetRateComparator.compare(o1.rate, o2.rate)
    }
    private val dexAssetRateComparator = Comparator<DexAssetRate> { o1, o2 ->
        o1.tokenEntity.name.compareTo(o2.tokenEntity.name)
    }


    suspend fun emulateSwap(
        sendAsset: DexAssetBalance,
        receiveAsset: DexAssetBalance,
        amount: BigDecimal,
        slippageTolerancePercent: Int
    ) = flow {
        emit(SwapSimulation.Loading)
        kotlinx.coroutines.delay(1000L)
        val result = withContext(Dispatchers.IO) {
            api.dex.dexSimulateSwap(
                sendAsset.contractAddress,
                receiveAsset.contractAddress,
                amount.movePointRight(sendAsset.decimals)
                    .setScale(0, RoundingMode.FLOOR)
                    .toPlainString(),
                BigDecimal(slippageTolerancePercent).movePointLeft(2).toPlainString()
            )
        }
        emit(result.toBalance(sendAsset, receiveAsset))
    }

    private fun DexReverseSimulateSwap200Response.toBalance(
        sentAsset: DexAssetBalance,
        receiveAsset: DexAssetBalance
    ): SwapSimulation.Result {
        return SwapSimulation.Result(
            exchangeRate = BigDecimal(swapRate),
            priceImpact = BigDecimal(priceImpact),
            minimumReceivedAmount = Coin.toCoins(minAskUnits, receiveAsset.decimals),
            receivedAsset = receiveAsset,
            liquidityProviderFee = Coin.toCoins(feeUnits, receiveAsset.decimals),
            sentAsset = sentAsset,
            blockchainFee = sentAsset.getRecommendedGasValues(receiveAsset)
        )
    }


    private fun AssetInfoSchema.isValid(): Boolean {
        return dexPriceUsd != null &&
                !blacklisted &&
                !deprecated &&
                !community &&
                imageUrl?.isNotBlank() == true &&
                displayName?.isNotBlank() == true
    }

    private fun AssetInfoSchema.toBalance(
        tonToUsdRate: RateEntity,
        tonToCurrencyRate: RateEntity
    ): DexAssetBalance {
        val tokenEntity = toTokenEntity()
        val dexPriceUsd = BigDecimal(dexPriceUsd)
        val dexPriceCurrency = dexPriceUsd / tonToUsdRate.value * tonToCurrencyRate.value
        return DexAssetBalance(
            type = kind.toBalance(),
            balance = balance?.let { Coin.toCoins(it, tokenEntity.decimals) } ?: BigDecimal.ZERO,
            rate = DexAssetRate(
                tokenEntity = tokenEntity,
                currency = tonToCurrencyRate.currency,
                rate = dexPriceCurrency
            )
        )
    }

    private fun AssetInfoSchema.toTokenEntity(): TokenEntity {
        return TokenEntity(
            address = contractAddress,
            name = displayName!!,
            symbol = symbol,
            imageUri = Uri.parse(imageUrl!!),
            decimals = decimals,
            verification = verification()
        )
    }

    private fun AssetInfoSchema.verification(): TokenEntity.Verification {
        return if (community) {
            TokenEntity.Verification.whitelist
        } else {
            TokenEntity.Verification.none
        }
    }

    private fun AssetKindSchema.toBalance(): DexAssetType {
        return when (this) {
            AssetKindSchema.Jetton -> DexAssetType.JETTON
            AssetKindSchema.Wton -> DexAssetType.WTON
            AssetKindSchema.Ton -> DexAssetType.TON
        }
    }
}