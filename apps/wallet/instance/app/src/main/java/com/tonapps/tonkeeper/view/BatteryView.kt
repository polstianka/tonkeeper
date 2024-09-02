package com.tonapps.tonkeeper.view

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
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
import com.tonapps.uikit.color.accentBlueColor
import com.tonapps.uikit.color.accentGreenColor
import com.tonapps.uikit.color.accentOrangeColor
import com.tonapps.uikit.color.iconSecondaryColor
import com.tonapps.uikit.color.resolveColor
import uikit.extensions.dp
import uikit.extensions.drawable
import uikit.extensions.useAttributes

class BatteryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class EmptyState(val value: Int) {
        NONE(0),
        SECONDARY(1),
        ACCENT(2),
        ACCENT_GREEN(3);

        companion object {
            fun from(value: Int): EmptyState {
                return entries.firstOrNull { it.value == value } ?: NONE
            }
        }
    }

    private var isInitialSet = true
    private var batteryLevel = 0f

    var emptyState: EmptyState = EmptyState.NONE
        set(value) {
            field = value
            invalidate()
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

    private var levelAnimator: ValueAnimator? = null
    private val iconDrawable = context.drawable(R.drawable.ic_flash_48)

    private val outerRect = RectF()
    private val capRect = RectF()
    private val innerRect = RectF()
    private val levelRect = RectF()
    private val iconRect = RectF()

    private var outerRadius: Float = 0f
    private var innerRadius: Float = 0f
    private var levelRadius: Float = 0f
    private var borderSize: Float = 0f
    private var capSize: Float = 0f

    private val capLeft: Float
        get() = (width.toFloat() - capSize) / 2

    private var capRadius: Float = 0f
    private var capOffset: Float = 0f
    private var iconSize: Float = 0f

    init {
        context.useAttributes(attrs, R.styleable.BatteryView) {
            emptyState = EmptyState.from(
                it.getInt(
                    R.styleable.BatteryView_emptyState,
                    EmptyState.NONE.value
                )
            )

            outerRadius = it.getDimension(R.styleable.BatteryView_outerRadius, 0f)
            innerRadius = it.getDimension(R.styleable.BatteryView_innerRadius, 0f)
            levelRadius = it.getDimension(R.styleable.BatteryView_levelRadius, 0f)
            borderSize = it.getDimension(R.styleable.BatteryView_borderSize, 0f)
            capSize = it.getDimension(R.styleable.BatteryView_capSize, 0f)
            capRadius = it.getDimension(R.styleable.BatteryView_capRadius, 0f)
            capOffset = it.getDimension(R.styleable.BatteryView_capOffset, 0f)
            iconSize = it.getDimension(R.styleable.BatteryView_iconSize, 0f)
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

            levelPaint.color = if (batteryLevel > MIN_LEVEL) {
                context.accentBlueColor
            } else {
                context.accentOrangeColor
            }

            canvas.drawRoundRect(levelRect, levelRadius, levelRadius, levelPaint)
        } else if (emptyState != EmptyState.NONE) {

            val color = when (emptyState) {
                EmptyState.SECONDARY -> context.iconSecondaryColor
                EmptyState.ACCENT -> context.accentBlueColor
                EmptyState.ACCENT_GREEN -> context.accentGreenColor
                else -> Color.TRANSPARENT
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

    // Function to set battery level
    fun setBatteryLevel(level: Float) {
        levelAnimator?.cancel()

        val nextValue = level.coerceIn(0f, 1f)
        
        if (level == 0f || isInitialSet) {
            if (isInitialSet) {
                isInitialSet = false
            }
            batteryLevel = nextValue
            invalidate()
            return
        }

        levelAnimator = ValueAnimator.ofFloat(batteryLevel, nextValue).apply {
            duration = 400 // Animation duration in milliseconds
            addUpdateListener { animation ->
                batteryLevel = animation.animatedValue as Float
                invalidate() // Trigger a redraw
            }
            start()
        }
    }

    companion object {
        private const val MIN_LEVEL = 0.14f
    }
}