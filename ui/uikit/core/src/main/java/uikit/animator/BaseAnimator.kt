package uikit.animator

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.os.Build
import androidx.core.animation.doOnEnd

abstract class BaseAnimator<T>(
    val duration: Long,
    val interpolator: TimeInterpolator,
    initialValue: T,
    private val onAnimationsFinished: ((finalValue: T, byAnimationEnd: Boolean) -> Unit)? = null,
    private val onValueChange: (T) -> Unit) {

    var value: T = initialValue
        private set(value) {
            if (field != value) {
                field = value
                onValueChange(value)
            }
        }
    val finalValue: T
        get() = if (isAnimating) {
            animatingToValue
        } else {
            value
        }
    var forcedValue: T
        get() = value
        set(newValue) = changeValue(newValue, false)
    var animatedValue: T
        get() = value
        set(newValue) = changeValue(newValue, true)

    val isAnimating: Boolean
        get() = animator != null
    private var animator: ValueAnimator? = null
    private var animatingToValue: T = initialValue

    fun stopAnimation(): Boolean {
        return animator?.let {
            it.cancel()
            animator = null
            true
        } ?: false
    }

    abstract fun interpolate(fromValue: T, toValue: T, fraction: Float): T

    @JvmOverloads
    fun changeValue(newValue: T, animated: Boolean = true) {
        if (animated && isAnimating && animatingToValue == newValue)
            return
        stopAnimation()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!ValueAnimator.areAnimatorsEnabled()) {
                value = newValue
                onAnimationsFinished?.invoke(animatedValue, false)
                return
            }
        }
        if (animated) {
            animatingToValue = newValue
            val animator = ValueAnimator.ofFloat(0f, 1f)
            animator.duration = duration
            animator.interpolator = interpolator
            val fromValue = this.value
            animator.addUpdateListener {
                if (this.animator == it) {
                    val fraction = it.animatedFraction
                    value = interpolate(fromValue, newValue, fraction)
                }
            }
            animator.doOnEnd {
                if (this.animator == it && stopAnimation()) {
                    onAnimationsFinished?.invoke(animatedValue, true)
                }
            }
            this.animator = animator
            animator.start()
        } else {
            value = newValue
            onAnimationsFinished?.invoke(animatedValue, false)
        }
    }
}