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
import com.tonapps.uikit.color.fieldErrorBackgroundColor
import com.tonapps.uikit.color.fieldErrorBorderColor
import uikit.ArgbEvaluator
import uikit.HapticHelper
import uikit.R
import uikit.base.BaseDrawable
import uikit.extensions.dp
import uikit.extensions.getDimension

class InputDrawable(
    private val context: Context
): BaseDrawable() {

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 160
    }

    private val cornerRadius = context.getDimension(R.dimen.cornerMedium)
    private val borderSize = 1.5f.dp
    private val backgroundColor = context.fieldBackgroundColor
    private val borderColor = context.fieldActiveBorderColor

    private val errorBackgroundColor = context.fieldErrorBackgroundColor
    private val errorBorderColor = context.fieldErrorBorderColor

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = backgroundColor
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.TRANSPARENT
        strokeWidth = borderSize
        style = Paint.Style.STROKE
    }

    private var animationsEnabled: Boolean = true

    var error: Boolean = false
        set(value) {
            if (value != field) {
                field = value
                updateState(animationsEnabled)
                if (value) {
                    HapticHelper.warning(context)
                }
            }
        }

    var active: Boolean = false
        set(value) {
            if (value != field) {
                field = value
                updateState(animationsEnabled)
            }
        }

    fun forceActive(value: Boolean) {
        if (value != active) {
            val oldValue = animationsEnabled
            animationsEnabled = false
            active = value
            animationsEnabled = oldValue
        }
    }

    private val boundF = RectF()

    var needBackground: Boolean = true

    override fun draw(canvas: Canvas) {
        if (needBackground) {
            drawBackground(canvas)
        }
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

    private fun updateState(animated: Boolean = true) {
        val oldBackgroundColor = backgroundPaint.color
        val oldBorderColor = borderPaint.color

        val newBackgroundColor: Int
        val newBorderColor: Int
        if (error) {
            newBackgroundColor = errorBackgroundColor
            newBorderColor = errorBorderColor
        } else if (active) {
            newBackgroundColor = backgroundColor
            newBorderColor = borderColor
        } else {
            newBackgroundColor = backgroundColor
            newBorderColor = Color.TRANSPARENT
        }

        backgroundPaint.color = newBackgroundColor
        borderPaint.color = newBorderColor
        if (animated) {
            animator.cancel()
            animator.addUpdateListener {
                val value = it.animatedValue as Float
                backgroundPaint.color = ArgbEvaluator.instance.evaluate(value, oldBackgroundColor, newBackgroundColor)
                borderPaint.color = ArgbEvaluator.instance.evaluate(value, oldBorderColor, newBorderColor)
                invalidateSelf()
            }
            animator.start()
        } else {
            invalidateSelf()
        }
    }

}