package uikit.extensions

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.DimenRes
import androidx.core.util.TypedValueCompat
import uikit.R
import kotlin.math.roundToInt

val Int.dp: Int
    get() {
        // return TypedValueCompat.pxToDp(this.toFloat(), Resources.getSystem().displayMetrics).roundToInt()
        val density = Resources.getSystem().displayMetrics.density
        return (this * density).roundToInt()
    }

val Float.dp: Float
    get() {
        // return TypedValueCompat.pxToDp(this, Resources.getSystem().displayMetrics)
        val density = Resources.getSystem().displayMetrics.density
        return (this * density)
    }

val Float.sp: Float
    get() {
        // return TypedValueCompat.pxToSp(this, Resources.getSystem().displayMetrics)
        val density = Resources.getSystem().displayMetrics.scaledDensity
        return (this * density)
    }

fun Context.dimen(@DimenRes id: Int): Int {
    return getDimensionPixelSize(id)
}

fun Context.dimenF(@DimenRes id: Int): Float {
    return getDimension(id)
}