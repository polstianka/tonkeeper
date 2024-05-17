package com.tonapps.tonkeeper.fragment.swap.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class Slippage(val value: Float): Parcelable {
    data class SlippageCustom(val custom: Float): Slippage(custom)
    data object Slippage1Percent: Slippage(0.01f)
    data object Slippage3Percent: Slippage(0.03f)
    data object Slippage5Percent: Slippage(0.05f)
}
