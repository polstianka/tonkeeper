package com.tonapps.tonkeeper.view

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.toRect
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.UIKitColor
import com.tonapps.uikit.color.resolveColor
import uikit.extensions.dp
import uikit.extensions.useAttributes

class BatteryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class EmptyState(val value: Int) {
        NONE(0),
        SECONDARY(1),
        ACCENT(2);

        companion object {
            fun from(value: Int): EmptyState {
                return values().firstOrNull { it.value == value } ?: NONE
            }
        }
    }

    private var isInitialSet = true
    private var batteryLevel = 0f
    private var emptyState: EmptyState = EmptyState.NONE

    private val dpHeight: Float
        get() {
            val density = Resources.getSystem().displayMetrics.density
            return height.toFloat() / density
        }

    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.resolveColor(UIKitColor.iconTertiaryColor)
        style = Paint.Style.FILL
    }
    private val levelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val iconDrawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_flash_48)

    private val outerRect = RectF()
    private val capRect = RectF()
    private val innerRect = RectF()
    private val levelRect = RectF()
    private val iconRect = RectF()

    private val outerRadius: Float
        get() = when {
            dpHeight >= 114 -> 16f.dp
            dpHeight >= 44 -> 6.5f.dp
            dpHeight >= 34 -> 5f.dp
            dpHeight >= 24 -> 3.5f.dp
            else -> 0f
        }

    private val innerRadius: Float
        get() = when {
            dpHeight >= 114 -> 12f.dp
            dpHeight >= 44 -> 5f.dp
            dpHeight >= 34 -> 3.5f.dp
            dpHeight >= 24 -> 2.5f.dp
            else -> 0f
        }

    private val levelRadius: Float
        get() = when {
            dpHeight >= 114 -> 8f.dp
            dpHeight >= 44 -> 3.5f.dp
            dpHeight >= 34 -> 2f.dp
            dpHeight >= 24 -> 1.5f.dp
            else -> 0f
        }

    private val borderSize: Float
        get() = when {
            dpHeight >= 114 -> 4f.dp
            dpHeight >= 44 -> 1.5f.dp
            dpHeight >= 34 -> 1.5f.dp
            dpHeight >= 24 -> 1f.dp
            else -> 0f
        }

    private val capSize: Float
        get() = when {
            dpHeight >= 114 -> 22f.dp
            dpHeight >= 44 -> 9f.dp
            dpHeight >= 34 -> 7f.dp
            dpHeight >= 24 -> 2.5f.dp
            else -> 0f
        }

    private val capLeft: Float
        get() = (width.toFloat() - capSize) / 2

    private val capRadius: Float
        get() = when {
            dpHeight >= 114 -> 14f.dp
            dpHeight >= 44 -> 10f.dp
            dpHeight >= 34 -> 4f.dp
            dpHeight >= 24 -> 3f.dp
            else -> 0f
        }

    private val capOffset: Float
        get() = when {
            dpHeight >= 114 -> 10f.dp
            dpHeight >= 44 -> 4f.dp
            dpHeight >= 34 -> 3f.dp
            dpHeight >= 24 -> 2f.dp
            else -> 0f
        }

    private val iconSize: Float
        get() = when {
            dpHeight >= 114 -> 48f.dp
            dpHeight >= 44 -> 22f.dp
            dpHeight >= 34 -> 16f.dp
            dpHeight >= 24 -> 12f.dp
            else -> 0f
        }

    init {
        context.useAttributes(attrs, R.styleable.BatteryView) {
            emptyState = EmptyState.from(
                it.getInt(
                    R.styleable.BatteryView_emptyState,
                    EmptyState.NONE.value
                )
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        val saveCount = canvas.saveLayer(null, null)

        outerRect.set(0f, capOffset, width, height)
        canvas.drawRoundRect(outerRect, outerRadius, outerRadius, borderPaint)

        capRect.set(capLeft, 0f, capLeft + capSize, capSize)
        canvas.drawRoundRect(capRect, capRadius, capRadius, borderPaint)

        innerRect.set(
            outerRect.left + borderSize,
            outerRect.top + borderSize,
            outerRect.right - borderSize,
            outerRect.bottom - borderSize
        )
        canvas.drawRoundRect(innerRect, innerRadius, innerRadius, maskPaint)

        if (batteryLevel > 0) {
            val levelRectHeight = innerRect.height() - borderSize * 2
            val levelHeight =
                if (batteryLevel > MIN_LEVEL) {
                    levelRectHeight * batteryLevel
                } else levelRadius * 2
            levelRect.set(
                innerRect.left + borderSize,
                innerRect.top + borderSize + (levelRectHeight - levelHeight),
                innerRect.right - borderSize,
                innerRect.bottom - borderSize
            )
            levelPaint.color =
                if (batteryLevel > MIN_LEVEL) {
                    context.resolveColor(UIKitColor.accentBlueColor)
                } else {
                    context.resolveColor(UIKitColor.accentOrangeColor)
                }
            canvas.drawRoundRect(levelRect, levelRadius, levelRadius, levelPaint)
        } else if (emptyState != EmptyState.NONE && iconDrawable != null) {
            val color = when (emptyState) {
                EmptyState.SECONDARY -> context.resolveColor(UIKitColor.iconSecondaryColor)
                EmptyState.ACCENT -> context.resolveColor(UIKitColor.accentBlueColor)
                else -> 0
            }
            val left = innerRect.left + (innerRect.width() - iconSize) / 2
            val top = innerRect.top + (innerRect.height() - iconSize) / 2
            iconRect.set(left, top, left + iconSize, top + iconSize)
            iconDrawable.bounds = iconRect.toRect()
            iconDrawable.setTint(color)
            iconDrawable.draw(canvas)
        }

        canvas.restoreToCount(saveCount)
    }

    private var animator: ValueAnimator? = null

    // Function to set battery level
    fun setBatteryLevel(level: Float) {
        if (animator?.isRunning == true) {
            animator!!.cancel()
        }

        val nextValue = level.coerceIn(0f, 1f)
        
        if (level.equals(0f) || isInitialSet) {
            if (isInitialSet) {
                isInitialSet = false
            }
            batteryLevel = nextValue
            invalidate()
            return
        }

        animator = ValueAnimator.ofFloat(batteryLevel, nextValue).apply {
            duration = 400 // Animation duration in milliseconds
            addUpdateListener { animation ->
                batteryLevel = animation.animatedValue as Float
                invalidate() // Trigger a redraw
            }
        }
        animator!!.start()
    }

    fun setEmptyState(state: EmptyState) {
        emptyState = state
        invalidate()
    }

    companion object {
        private const val MIN_LEVEL = 0.14f
    }
}