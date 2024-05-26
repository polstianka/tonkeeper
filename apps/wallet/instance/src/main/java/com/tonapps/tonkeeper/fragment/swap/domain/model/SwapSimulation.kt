package com.tonapps.tonkeeper.fragment.swap.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

sealed class SwapSimulation {
    object Loading : SwapSimulation()
    @Parcelize
    data class Result(
        val exchangeRate: BigDecimal,
        val priceImpact: BigDecimal,
        val minimumReceivedAmount: BigDecimal,
        val receivedAsset: DexAssetBalance,
        val sentAsset: DexAssetBalance,
        val liquidityProviderFee: BigDecimal,
        val blockchainFee: BigDecimal
    ) : SwapSimulation(), Parcelable
}