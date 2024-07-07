package com.tonapps.tonkeeper.view

import android.content.res.Resources
import android.util.DisplayMetrics
import kotlin.math.floor

object Screen {
    fun dp(dp: Int): Int {
        return dp(dp.toFloat())
    }

    fun dp(dp: Float): Int {
        val scale: Float = density()
        return floor((dp * scale).toDouble()).toInt()
    }

    fun sp(sp: Int): Int {
        return sp(sp.toFloat())
    }

    fun sp(sp: Float): Int {
        return spFloat(sp).toInt()
    }

    fun spFloat(sp: Float): Float {
        val scale: Float = getDisplayMetrics().scaledDensity
        return sp * scale + 0.5f
    }

    fun density(): Float {
        return getDisplayMetrics().density
    }

    fun getDisplayMetrics(): DisplayMetrics {
        return Resources.getSystem().displayMetrics
    }
}