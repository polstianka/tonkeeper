package uikit.animator

import android.animation.TimeInterpolator

class FloatAnimator(
    duration: Long,
    interpolator: TimeInterpolator,
    initialValue: Float = 0.0f,
    onAnimationsFinished: ((finalValue: Float, byAnimationEnd: Boolean) -> Unit)? = null,
    onApplyValue: (Float) -> Unit
) : BaseAnimator<Float>(duration, interpolator, initialValue, onAnimationsFinished, onApplyValue) {
    override fun interpolate(fromValue: Float, toValue: Float, fraction: Float) =
        fromValue + (toValue - fromValue) * fraction
}