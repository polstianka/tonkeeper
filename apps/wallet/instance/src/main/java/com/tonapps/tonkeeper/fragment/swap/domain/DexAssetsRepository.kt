package com.tonapps.tonkeeper.fragment.swap.domain

import android.net.Uri
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAsset
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAssetType
import com.tonapps.tonkeeper.fragment.swap.domain.model.SwapSimulation
import com.tonapps.tonkeeper.fragment.swap.domain.model.getRecommendedGasValues
import com.tonapps.wallet.api.StonfiAPI
import com.tonapps.wallet.api.entity.TokenEntity
import io.stonfiapi.models.AssetInfoSchema
import io.stonfiapi.models.AssetKindSchema
import io.stonfiapi.models.DexReverseSimulateSwap200Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode

class DexAssetsRepository(
    private val api: StonfiAPI
) {

    private val _isLoading = MutableStateFlow(false)
    private val _items = MutableStateFlow<List<DexAsset>>(emptyList())

    val isLoading: StateFlow<Boolean>
        get() = _isLoading
    val items: Flow<List<DexAsset>>
        get() = _items
    val positiveBalance = items.map { it.filter { it.balance > BigDecimal.ZERO } }

    suspend fun loadAssets(
        walletAddress: String
    ) = withContext(Dispatchers.IO) {
        _isLoading.value = true
        val response = api.wallets.getWalletAssets(walletAddress)
        _items.value = response.assetList
            .asSequence()
            .filter { it.isValid() }
            .map { it.toDomain() }
            .sortedWith(dexAssetComparator)
            .toList()

        _isLoading.value = false
    }

    fun getDefaultAsset(): DexAsset {
        return _items.value.first { it.type == DexAssetType.TON }
    }

    private val dexAssetComparator = Comparator<DexAsset> { o1, o2 ->
        val f1 = o1.balance * o1.dexUsdPrice
        val f2 = o2.balance * o2.dexUsdPrice
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
            minimumReceivedAmount = BigDecimal(minAskUnits).movePointLeft(receiveAsset.decimals),
            receivedAsset = receiveAsset,
            liquidityProviderFee = BigDecimal(feeUnits).movePointLeft(receiveAsset.decimals),
            sentAsset = sentAsset,
            blockchainFee = sentAsset.getRecommendedGasValues(receiveAsset)
        )
    }


    private fun AssetInfoSchema.isValid(): Boolean {
        return dexPriceUsd != null &&
                !blacklisted &&
                !deprecated &&
                imageUrl?.isNotBlank() == true &&
                displayName?.isNotBlank() == true
    }

    private fun AssetInfoSchema.toDomain(): DexAsset {
        return DexAsset(
            hasDefaultSymbol = defaultSymbol,
            type = kind.toDomain(),
            dexUsdPrice = BigDecimal(dexPriceUsd),
            balance = balance?.toBigDecimalOrNull() ?: BigDecimal.ZERO,
            tokenEntity = toTokenEntity()
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