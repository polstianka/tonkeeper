package com.tonapps.tonkeeper.fragment.swap.domain

import com.tonapps.tonkeeper.fragment.swap.domain.model.AssetBalance
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAsset
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAssetType
import com.tonapps.tonkeeper.fragment.swap.domain.model.SwapSimulation
import com.tonapps.tonkeeper.fragment.swap.domain.model.getRecommendedGasValues
import com.tonapps.tonkeeper.fragment.swap.domain.model.recommendedForwardTon
import com.tonapps.wallet.api.StonfiAPI
import io.stonfiapi.models.AssetInfoSchema
import io.stonfiapi.models.AssetKindSchema
import io.stonfiapi.models.DexReverseSimulateSwap200Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode

class DexAssetsRepository(
    private val api: StonfiAPI
) {

    private val _isLoading = MutableStateFlow(false)
    private val _communityItems = MutableStateFlow<List<DexAsset>>(emptyList())
    private val _nonCommunityItems = MutableStateFlow<List<DexAsset>>(emptyList())

    val communityItems: StateFlow<List<DexAsset>>
        get() = _communityItems
    val nonCommunityItems: StateFlow<List<DexAsset>>
        get() = _nonCommunityItems
    val isLoading: StateFlow<Boolean>
        get() = _isLoading

    suspend fun loadAssets() = withContext(Dispatchers.IO) {
        _isLoading.value = true
        val response = api.dex.getAssetList()
        val community = mutableListOf<AssetInfoSchema>()
        val notCommunity = mutableListOf<AssetInfoSchema>()
        response.assetList.forEach { asset ->
            when {
                !asset.isValid() -> Unit
                asset.community -> community.add(asset)
                else -> notCommunity.add(asset)
            }
        }
        _communityItems.value = community.map { it.toDomain() }
        _nonCommunityItems.value = notCommunity.map { it.toDomain() }

        _isLoading.value = false
    }

    fun getDefaultAsset(): DexAsset {
        return nonCommunityItems.value.first { it.type == DexAssetType.TON }
    }


    private val assetBalances = mutableListOf<AssetBalance.Entity>()
    fun getAssetBalance(
        walletAddress: String,
        asset: DexAsset
    ) = flow {
        if (assetBalances.isEmpty()) {
            emit(AssetBalance.Loading)
            val assets = withContext(Dispatchers.IO) {
                api.wallets.getWalletAssets(walletAddress)
                    .assetList
                    .filter { it.isValid() && it.balance != null }
                    .map {
                        val c = it.toDomain()
                        val a = it.balance?.toBigDecimalOrNull() ?: BigDecimal.ZERO
                        val b = a.movePointLeft(c.decimals)
                        AssetBalance.Entity(c, b)
                    }
            }
            assetBalances.clear()
            assetBalances.addAll(assets)
        }
        val balance = assetBalances.firstOrNull { it.asset.contractAddress == asset.contractAddress }
        emit(balance)
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
            minimumReceivedAmount = BigDecimal(minAskUnits).movePointLeft(receiveAsset.decimals),
            receivedAsset = receiveAsset,
            liquidityProviderFee = BigDecimal(feeUnits).movePointLeft(receiveAsset.decimals),
            sentAsset = sentAsset,
            blockchainFee = sentAsset.getRecommendedGasValues(receiveAsset)
        )
    }


    private fun AssetInfoSchema.isValid(): Boolean {
        return dexPriceUsd != null && !blacklisted && !deprecated
    }

    private fun AssetInfoSchema.toDomain(): DexAsset {
        return DexAsset(
            isCommunity = community,
            contractAddress = contractAddress,
            decimals = decimals,
            hasDefaultSymbol = defaultSymbol,
            type = kind.toDomain(),
            symbol = symbol,
            imageUrl = imageUrl!!,
            displayName = displayName!!,
            dexUsdPrice = BigDecimal(dexPriceUsd)
        )
    }

    private fun AssetKindSchema.toDomain(): DexAssetType {
        return when (this) {
            AssetKindSchema.Jetton -> DexAssetType.JETTON
            AssetKindSchema.Wton -> DexAssetType.WTON
            AssetKindSchema.Ton -> DexAssetType.TON
        }
    }
}