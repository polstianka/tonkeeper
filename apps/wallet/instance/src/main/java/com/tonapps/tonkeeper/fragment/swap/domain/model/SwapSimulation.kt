package com.tonapps.tonkeeper.fragment.swap.domain.model

import java.math.BigDecimal

sealed class SwapSimulation {
    object Loading : SwapSimulation()
    data class Result(
        val exchangeRate: BigDecimal,
        val priceImpact: BigDecimal,
        val minimumReceivedAmount: BigDecimal,
        val receivedAsset: DexAsset,
        val sentAsset: DexAsset,
        val liquidityProviderFee: BigDecimal,
        val blockchainFee: BigDecimal
    ) : SwapSimulation()
}