package uikit.drawable

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import com.tonapps.uikit.color.accentBlueColor
import com.tonapps.uikit.color.fieldActiveBorderColor
import com.tonapps.uikit.color.fieldBackgroundColor
import com.tonapps.uikit.color.fieldErrorBackgroundColor
import com.tonapps.uikit.color.fieldErrorBorderColor
import uikit.ArgbEvaluator
import uikit.HapticHelper
import uikit.R
import uikit.base.BaseDrawable
import uikit.extensions.dp
import uikit.extensions.getDimension
import uikit.extensions.range

class TabDrawable(
    context: Context
): BaseDrawable() {

    private var animator: ValueAnimator? = null
    private var progress = 0f

    private val borderSize = 3f.dp
    private val borderColor = context.accentBlueColor

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = borderColor
        style = Paint.Style.FILL
    }

    private var isActive: Boolean = false

    fun setActive(active: Boolean) {
        setActive(active, true)
    }

    fun setActive(active: Boolean, animated: Boolean) {
        if (isActive != active) {
            isActive = active
            if (animated) {
                updateState()
            } else {
                progress = if (active) 1f else 0f
                invalidateSelf()
            }
        }
    }

    private val boundF = RectF()

    override fun draw(canvas: Canvas) {
        drawBorder(canvas)
    }

    private fun drawBorder(canvas: Canvas) {
        if (progress == 0f) {
            return
        }

        canvas.drawRoundRect(
            progress.range(boundF.centerX(), boundF.left),
            boundF.bottom - borderSize,
            progress.range(boundF.centerX(), boundF.right),
            boundF.bottom,
            borderSize / 2f,
            borderSize / 2f,
            borderPaint
        )
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        boundF.set(bounds)
    }



    private fun updateState() {
        animator?.cancel()

        val start = progress
        val end = if (isActive) 1f else 0f

        val anim = ValueAnimator.ofFloat(start, end).apply {
            duration = 160
        }

        animator = anim
        anim.addUpdateListener {
            progress = it.animatedValue as Float
            invalidateSelf()
        }
        anim.start()
    }
}