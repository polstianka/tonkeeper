package com.tonapps.tonkeeper.view

import android.content.Context
import android.util.AttributeSet
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.core.view.WindowInsetsCompat
import uikit.extensions.setPaddingBottom
import kotlin.math.max

class InsetsFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle) {

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        val compatInsets = WindowInsetsCompat.toWindowInsetsCompat(insets)
        val navigationInsets = compatInsets.getInsets(WindowInsetsCompat.Type.ime())
        setPaddingBottom(max(navigationInsets.bottom, compatInsets.stableInsetBottom))
        return super.onApplyWindowInsets(insets)
    }
}