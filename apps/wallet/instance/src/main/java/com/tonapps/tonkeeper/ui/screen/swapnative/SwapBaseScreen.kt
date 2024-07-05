package com.tonapps.tonkeeper.ui.screen.swapnative

import androidx.annotation.LayoutRes
import com.tonapps.uikit.color.accentOrangeColor
import com.tonapps.uikit.color.accentRedColor
import com.tonapps.uikit.color.textPrimaryColor
import uikit.base.BaseFragment

open class SwapBaseScreen(
    @LayoutRes layoutId: Int
) : BaseFragment(layoutId) {


    fun getPriceImpactColor(priceImpact: Float): Int {
        return if (priceImpact <= 1.0f) {
            requireContext().textPrimaryColor
        } else if (1.0f < priceImpact && priceImpact <= 5.0f) {
            requireContext().accentOrangeColor
        } else {
            requireContext().accentRedColor
        }
    }

}