package uikit.effect

import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import uikit.animator.BoolAnimator
import uikit.extensions.scale
import kotlin.math.max
import kotlin.math.min

class FadeAndScaleVisibilityEffect(
    vararg val targets: View?,
    initialValue: Boolean = true,
    private val goneVisibility: Int = DEFAULT_GONE_VISIBILITY,
    private val goneScale: Float = DEFAULT_GONE_SCALE,
    onApplyEffect: ((animatedValue: Float) -> Unit)? = null
) : BaseEffect(*targets) {

    companion object {
        const val DURATION = 180L
        const val DEFAULT_GONE_SCALE: Float = .8f
        const val DEFAULT_GONE_VISIBILITY: Int = View.GONE

        fun View.applyFadeAndScaleVisibilityAffect(visibility: Float, goneVisibility: Int = DEFAULT_GONE_VISIBILITY, goneScale: Float = DEFAULT_GONE_SCALE, updateVisibility: Boolean = true) {
            val scale = goneScale + (1.0f - goneScale) * visibility
            val alpha = visibility.coerceIn(0.0f, 1.0f)

            this.scale = scale
            this.alpha = alpha
            if (updateVisibility) {
                this.visibility = if (alpha > 0f) {
                    View.VISIBLE
                } else {
                    goneVisibility
                }
            }
            if (this is TextView) {
                val layout = this.layout
                if (layout != null) {
                    val lineCount = layout.lineCount
                    if (lineCount > 0) {
                        var minLineLeft = 0.0f
                        var maxLineRight = 0.0f
                        for (lineIndex in 0 until lineCount) {
                            val lineLeft = layout.getLineLeft(lineIndex)
                            val lineRight = layout.getLineRight(lineIndex)
                            if (lineIndex == 0) {
                                minLineLeft = lineLeft
                                maxLineRight = lineRight
                            } else {
                                minLineLeft = min(lineLeft, minLineLeft)
                                maxLineRight = max(lineRight, maxLineRight)
                            }
                        }
                        this.pivotX = minLineLeft + (maxLineRight - minLineLeft) / 2.0f
                    } else {
                        this.pivotX = (this.paddingLeft + (this.measuredWidth - this.paddingLeft - this.paddingRight)).toFloat()
                    }
                }
            }
        }
    }

    val value: Boolean
        get() = animator.value
    val animatedValue: Float
        get() = animator.floatValue

    private val animator = BoolAnimator(DURATION, DecelerateInterpolator(), initialValue) { _, animatedValue, _, _ ->
        for (view in targets) {
            view?.applyFadeAndScaleVisibilityAffect(animatedValue, goneVisibility, goneScale)
        }
        onApplyEffect?.invoke(animatedValue)
    }

    fun setIsVisible(visible: Boolean, animated: () -> Boolean = { true }) {
        if (animated()) {
            animator.animatedValue = visible
        } else {
            animator.forcedValue = visible
        }
    }
}