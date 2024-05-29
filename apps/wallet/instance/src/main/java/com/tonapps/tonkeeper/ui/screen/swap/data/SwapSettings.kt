package com.tonapps.tonkeeper.ui.screen.swap.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class SwapSettings(
    val slippageTolerance: SlippageTolerance = DEFAULT_SLIPPAGE_TOLERANCE,
    val enableExpertMode: Boolean = false
): Parcelable {
    companion object {
        val DEFAULT_SLIPPAGE_TOLERANCE: SlippageTolerance = SlippageTolerance.valueOf(0.01)
        val MAX_SLIPPAGE_TOLERANCE: SlippageTolerance = SlippageTolerance.valueOf(0.5)
    }
}

typealias SlippageTolerance = BigDecimal

fun String.percentageToSlippageTolerance(): SlippageTolerance = this.toBigDecimal().movePointLeft(2).stripTrailingZeros()
fun String.percentageToSlippageToleranceOrNull(): SlippageTolerance? = this.toBigDecimalOrNull()?.movePointLeft(2)?.stripTrailingZeros()