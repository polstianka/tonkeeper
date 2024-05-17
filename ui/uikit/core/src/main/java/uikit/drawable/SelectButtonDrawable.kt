package uikit.drawable

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import com.tonapps.uikit.color.fieldActiveBorderColor
import com.tonapps.uikit.color.fieldBackgroundColor
import uikit.ArgbEvaluator
import uikit.R
import uikit.base.BaseDrawable
import uikit.extensions.dp
import uikit.extensions.getDimension

class SelectButtonDrawable(
    context: Context,
): BaseDrawable() {

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 160
    }

    private val cornerRadius = context.getDimension(R.dimen.cornerMedium)
    private val borderSize = 1.5f.dp
    private val backgroundColor = context.fieldBackgroundColor
    private val borderColor = context.fieldActiveBorderColor

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = backgroundColor
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.TRANSPARENT
        strokeWidth = borderSize
        style = Paint.Style.STROKE
    }


    private var selected: Boolean = false
        set(value) {
            if (value != field) {
                field = value
                updateState()
            }
        }

    private val boundF = RectF()

    override fun draw(canvas: Canvas) {
        drawBackground(canvas)
        drawBorder(canvas)
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawRoundRect(
            boundF,
            cornerRadius,
            cornerRadius,
            backgroundPaint
        )
    }

    private fun drawBorder(canvas: Canvas) {
        canvas.drawRoundRect(
            boundF.left + (borderSize / 2f),
            boundF.top + (borderSize / 2f),
            boundF.right - (borderSize / 2f),
            boundF.bottom - (borderSize / 2f),
            cornerRadius,
            cornerRadius,
            borderPaint
        )
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        boundF.set(bounds)
    }

    private fun updateState() {
        val oldBorderColor = borderPaint.color

        val newBorderColor: Int
        if (selected) {
            newBorderColor = borderColor
        } else {
            newBorderColor = Color.TRANSPARENT
        }

        borderPaint.color = newBorderColor

        animator.cancel()
        animator.addUpdateListener {
            val value = it.animatedValue as Float
            borderPaint.color = ArgbEvaluator.instance.evaluate(value, oldBorderColor, newBorderColor)
            invalidateSelf()
        }
        animator.start()
    }

    override fun isStateful(): Boolean {
        return true
    }

    override fun onStateChange(stateSet: IntArray): Boolean {
        var selected = false

        for (state in stateSet) {
            if (state == android.R.attr.state_selected) {
                selected = true
            }
        }
        return if (this.selected != selected) {
            this.selected = selected
            true
        } else {
            false
        }
    }

}