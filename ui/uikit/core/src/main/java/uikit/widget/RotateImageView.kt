package uikit.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.widget.AppCompatImageView
import uikit.animator.FloatAnimator

class RotateImageView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
) : AppCompatImageView(context, attrs, defStyle) {
    private val animator = FloatAnimator(235L, DecelerateInterpolator()) {
        invalidate()
    }

    private val allowAnimations: Boolean
        get() = alpha > 0.0f && visibility == VISIBLE && minOf(measuredWidth, measuredHeight) > 0

    var animatedRotation: Float
        get() = animator.animatedValue
        set(value) {
            animator.changeValue(value, allowAnimations)
        }

    val finalAnimatedRotation: Float
        get() = animator.finalValue

    var forcedRotation: Float
        get() = animator.forcedValue
        set(value) {
            animator.forcedValue = value
        }

    override fun onDraw(c: Canvas) {
        val rotation = animator.value % 360.0f
        if (rotation == 0.0f) {
            super.onDraw(c)
            return
        }
        val centerX = this.paddingLeft + (this.measuredWidth - this.paddingLeft - this.paddingRight).toFloat() / 2.0f
        val centerY = this.paddingTop + (this.measuredHeight - this.paddingTop - this.paddingBottom).toFloat() / 2.0f
        val saveCount = c.save()
        c.rotate(rotation, centerX, centerY)
        super.onDraw(c)
        c.restoreToCount(saveCount)
    }
}