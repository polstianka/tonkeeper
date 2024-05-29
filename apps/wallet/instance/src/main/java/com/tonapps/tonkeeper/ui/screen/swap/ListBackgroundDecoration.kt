package com.tonapps.tonkeeper.ui.screen.swap

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.uikit.color.UIKitColor
import com.tonapps.uikit.color.backgroundContentColor
import com.tonapps.uikit.color.backgroundHighlightedColor
import com.tonapps.uikit.color.resolveColor
import com.tonapps.uikit.color.separatorCommonColor
import uikit.base.BaseDrawable
import uikit.extensions.dp
import uikit.extensions.getDimension

class ListBackgroundDecoration(context: Context) : RecyclerView.ItemDecoration() {
    private val radius = 16.dp.toFloat()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.resolveColor(UIKitColor.backgroundContentColor)
    }

    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.resolveColor(UIKitColor.separatorCommonColor)
    }
    private val topCorners = floatArrayOf(radius, radius, radius, radius, 0f, 0f, 0f, 0f)
    private val bottomCorners = floatArrayOf(0f, 0f, 0f, 0f, radius, radius, radius, radius)
    private val allCorners = floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius)
    private val rect = RectF()
    private val path = Path()

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            rect.set(
                child.left.toFloat(),
                child.top.toFloat(),
                child.right.toFloat(),
                child.bottom.toFloat()
            )

            var drawDivider = true
            when (position) {
                0 -> {
                    path.reset()
                    if (parent.childCount == 1) {
                        path.addRoundRect(rect, allCorners, Path.Direction.CW)
                    } else {
                        path.addRoundRect(rect, topCorners, Path.Direction.CW)
                    }
                    canvas.drawPath(path, paint)
                    drawDivider = false
                }

                (parent.adapter?.itemCount ?: 0) - 1 -> {
                    path.reset()
                    path.addRoundRect(rect, bottomCorners, Path.Direction.CW)
                    canvas.drawPath(path, paint)
                }

                else -> canvas.drawRect(rect, paint)
            }
            if (drawDivider) {
                canvas.drawRect(
                    child.left + 16.dp.toFloat(),
                    child.top.toFloat(),
                    child.right.toFloat(),
                    child.top.toFloat() + 1.dp.toFloat(),
                    dividerPaint
                )
            }
        }
    }
}

class CellBackgroundDrawable(
    context: Context,
    private val position: Int,
    private val count: Int,
    backgroundColor: Int = context.backgroundContentColor
): BaseDrawable() {

    companion object {
        private fun createCorners(
            topLeft: Float,
            topRight: Float,
            bottomRight: Float,
            bottomLeft: Float
        ): FloatArray {
            return floatArrayOf(
                topLeft, topLeft,
                topRight, topRight,
                bottomRight, bottomRight,
                bottomLeft, bottomLeft
            )
        }

        fun create(
            context: Context,
            position: Int,
            count: Int,
            backgroundColor: Int = context.backgroundContentColor
        ): Drawable {
            val color = context.backgroundHighlightedColor
            return RippleDrawable(
                ColorStateList.valueOf(color),
                CellBackgroundDrawable(context, position, count, backgroundColor),
                null
            )
        }
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = backgroundColor
    }
    private val radius = context.getDimension(uikit.R.dimen.cornerMedium)
    private val firstCorners = createCorners(radius, radius, 0f, 0f)
    private val lastCorners = createCorners(0f, 0f, radius, radius)
    private val singleCorners = createCorners(radius, radius, radius, radius)

    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.separatorCommonColor
    }

    private val dividerHeight = .5f.dp
    private val dividerMarginStart = 16f.dp
    private val path = Path()

    override fun draw(canvas: Canvas) {
        canvas.drawPath(path, backgroundPaint)
        if ((position == 0 && count == 1).not() && (position == count - 1).not()) {
            drawDivider(canvas, bounds)
        }
    }

    private fun drawDivider(canvas: Canvas, bounds: Rect) {
        canvas.drawRect(
            bounds.left + dividerMarginStart,
            bounds.bottom - dividerHeight,
            bounds.right.toFloat(),
            bounds.bottom.toFloat(),
            dividerPaint
        )
    }

    private fun firstPath(bounds: Rect) {
        path.reset()
        path.addRoundRect(
            RectF(bounds),
            firstCorners,
            Path.Direction.CW
        )
    }

    private fun middlePath(bounds: Rect) {
        path.reset()
        path.addRect(RectF(bounds), Path.Direction.CW)
    }

    private fun lastPath(bounds: Rect) {
        path.reset()
        path.addRoundRect(
            RectF(bounds),
            lastCorners,
            Path.Direction.CW
        )
    }

    private fun singlePath(bounds: Rect) {
        path.reset()
        path.addRoundRect(
            RectF(bounds),
            singleCorners,
            Path.Direction.CW
        )
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        when {
            position == 0 && count > 1 -> firstPath(bounds)
            position == 0 && count == 1 -> singlePath(bounds)
            position == count - 1 -> lastPath(bounds)
            else -> middlePath(bounds)
        }
    }
}