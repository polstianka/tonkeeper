package com.tonapps.tonkeeper.fragment.swap.domain

import android.net.Uri
import com.tonapps.blockchain.Coin
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAsset
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAssetRate
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAssetType
import com.tonapps.tonkeeper.fragment.swap.domain.model.SwapSimulation
import com.tonapps.tonkeeper.fragment.swap.domain.model.getRecommendedGasValues
import com.tonapps.wallet.api.StonfiAPI
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.rates.entity.RateEntity
import io.stonfiapi.models.AssetInfoSchema
import io.stonfiapi.models.AssetKindSchema
import io.stonfiapi.models.DexReverseSimulateSwap200Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

class DexAssetsRepository(
    private val api: StonfiAPI,
    private val ratesRepository: RatesRepository
) {

    private val isLoadingFlows = mutableMapOf<String, MutableStateFlow<Boolean>>()
    private val assetsFlowLock = ReentrantReadWriteLock()
    private val assetsFlows = mutableMapOf<String, MutableStateFlow<List<DexAsset>>>()

    fun getIsLoadingFlow(walletAddress: String): Flow<Boolean> {
        return getIsLoadingMutableFlow(walletAddress)
    }

    fun getAssetsFlow(walletAddress: String): Flow<List<DexAsset>> {
        return getAssetsMutableFlow(walletAddress)
    }

    fun getPositiveBalanceFlow(walletAddress: String): Flow<List<DexAsset>> {
        return getAssetsFlow(walletAddress)
            .map { list -> list.filter { it.balance > BigDecimal.ZERO } }
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
            .map { it.toDomain(tonToUsdRate, tonToCurrencyRate) }
            .sortedWith(dexAssetComparator)
            .toList()

        isLoading.value = false
    }

    fun getDefaultAsset(walletAddress: String): DexAsset {
        val items = getAssetsMutableFlow(walletAddress)
        return items.value.first { it.type == DexAssetType.TON }
    }

    private val dexAssetComparator = Comparator<DexAsset> { o1, o2 ->
        val f1 = o1.balance * o1.rate.rate
        val f2 = o2.balance * o2.rate.rate
        f2.compareTo(f1)
            .takeUnless { it == 0 }
            ?: o1.displayName.compareTo(o2.displayName)
    }


    suspend fun emulateSwap(
        sendAsset: DexAsset,
        receiveAsset: DexAsset,
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
        emit(result.toDomain(sendAsset, receiveAsset))
    }

    private fun DexReverseSimulateSwap200Response.toDomain(
        sentAsset: DexAsset,
        receiveAsset: DexAsset
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

    private fun AssetInfoSchema.toDomain(
        tonToUsdRate: RateEntity,
        tonToCurrencyRate: RateEntity
    ): DexAsset {
        val tokenEntity = toTokenEntity()
        val dexPriceUsd = BigDecimal(dexPriceUsd)
        val dexPriceCurrency = dexPriceUsd / tonToUsdRate.value * tonToCurrencyRate.value
        return DexAsset(
            type = kind.toDomain(),
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

    private fun AssetKindSchema.toDomain(): DexAssetType {
        return when (this) {
            AssetKindSchema.Jetton -> DexAssetType.JETTON
            AssetKindSchema.Wton -> DexAssetType.WTON
            AssetKindSchema.Ton -> DexAssetType.TON
        }
    }
}