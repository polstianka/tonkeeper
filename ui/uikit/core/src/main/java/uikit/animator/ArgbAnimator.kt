package uikit.animator

import android.animation.TimeInterpolator
import android.graphics.Color
import androidx.annotation.ColorInt
import uikit.ArgbEvaluator

class ArgbAnimator(
    duration: Long,
    interpolator: TimeInterpolator,
    @ColorInt initialValue: Int = Color.BLACK,
    onAnimationsFinished: ((finalValue: Int, byAnimationEnd: Boolean) -> Unit)? = null,
    onApplyValue: (Int) -> Unit
) : BaseAnimator<Int>(duration, interpolator, initialValue, onAnimationsFinished, onApplyValue) {
    override fun interpolate(fromValue: Int, toValue: Int, fraction: Float): Int =
        ArgbEvaluator.instance.evaluate(fraction, fromValue, toValue)
}