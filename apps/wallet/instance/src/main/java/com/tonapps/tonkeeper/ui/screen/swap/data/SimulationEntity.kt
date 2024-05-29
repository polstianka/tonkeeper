package com.tonapps.tonkeeper.ui.screen.swap.data

import android.os.Parcelable
import com.tonapps.blockchain.Coin
import com.tonapps.tonkeeper.core.toPercentage
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.ton.bigint.BigInt
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * The class is allows potential alternative providers in future.
 */
@Parcelize
data class SimulationEntity(
    val addresses: SimulationAddresses,
    val send: SimulationNumber,
    val receive: SimulationNumber,
    val minReceived: SimulationNumber,
    val swapRate: BigDecimal,
    val priceImpact: BigDecimal,
    val fee: SimulationNumber,
    val slippageTolerance: SlippageTolerance,
    val providerName: String
) : Parcelable {
    @IgnoredOnParcel
    val priceImpactPercentage: BigDecimal = priceImpact.toPercentage(-1, RoundingMode.HALF_DOWN)

    @IgnoredOnParcel
    val priceImpactGrade: PriceImpactGrade = when {
        priceImpact <= SwapConfig.DEX_SWAP_GRADE_PRICE_IMPACT_PERCENT_LOW -> PriceImpactGrade.LOW
        priceImpact <= SwapConfig.DEX_SWAP_GRADE_PRICE_IMPACT_PERCENT_HIGH -> PriceImpactGrade.MEDIUM
        else -> PriceImpactGrade.HIGH
    }
}

enum class PriceImpactGrade {
    LOW, MEDIUM, HIGH
}

@Parcelize
data class SimulationAddresses (
    val offerAddress: String,
    val askAddress: String,
    val routerAddress: String,
    val poolAddress: String,
    val feeAddress: String
) : Parcelable

@Parcelize
data class SimulationNumber (
    val nano: BigInt,
    val coins: BigDecimal,
    val coinDecimals: Int
) : Parcelable {
    constructor(nano: BigInt, coinDecimals: Int) : this(nano, Coin.toCoins(nano, coinDecimals), coinDecimals)
    constructor(nano: String, coinDecimals: Int) : this(BigInt(nano), coinDecimals)
}