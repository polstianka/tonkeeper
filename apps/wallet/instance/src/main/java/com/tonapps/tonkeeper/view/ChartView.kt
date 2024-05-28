package com.tonapps.tonkeeper.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.core.view.doOnLayout
import com.tonapps.tonkeeper.api.chart.ChartEntity
import com.tonapps.uikit.color.accentBlueColor
import com.tonapps.uikit.color.accentGreenColor
import uikit.extensions.dp
import uikit.extensions.withAlpha


class ChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : View(context, attrs, defStyle) {

    private var data = listOf(ChartEntity(0, 0f))
    private var square = false
    private var labels = listOf<String>()
    private var minMaxPrice = listOf<String>()
    private var isStaking = false
    private var accentColor = context.accentBlueColor

    private val labelCoordinates = mutableListOf<Pair<Float, Float>>()
    private val priceCoordinates = mutableListOf<Pair<Float, Float>>()

    private val labelTextHeight = 40f
    private val priceTextHeight = 50f

    fun setData(
        data: List<ChartEntity>,
        square: Boolean,
        labels: List<String>,
        isStaking: Boolean,
        minMaxPrice: List<String>
    ) {
        this.data = data
        this.square = square
        this.labels = labels
        this.isStaking = isStaking
        this.minMaxPrice = minMaxPrice
        accentColor = if (isStaking) context.accentGreenColor else context.accentBlueColor
        doOnLayout {
            buildPath()
            invalidate()
        }
    }

    private val path = Path()
    private var verticalPaths = emptyList<Path>()

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 2f.dp
        style = Paint.Style.STROKE
    }

    private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val verticalLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 1f.dp
        style = Paint.Style.STROKE
        alpha = 137
    }

    private val labelTextPaint = Paint().apply {
        color = Color.GRAY
        textSize = labelTextHeight
    }

    private val priceTextPaint = Paint().apply {
        color = Color.GRAY
        textSize = priceTextHeight
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        linePaint.color = accentColor
        gradientPaint.color = accentColor
        verticalLinePaint.color = accentColor

        val lineWidthPart = linePaint.strokeWidth / 2f
        canvas.translate(-lineWidthPart, lineWidthPart)
        canvas.drawPath(path, linePaint)
        verticalPaths.forEach {
            canvas.drawPath(it, verticalLinePaint)
        }
        labelCoordinates.forEachIndexed { index, it ->
            canvas.drawText(labels[index], it.first, it.second, labelTextPaint)
        }
        priceCoordinates.forEachIndexed { index, it ->
            canvas.drawText(minMaxPrice[index], it.first, it.second, priceTextPaint)
        }
        canvas.drawPath(path, gradientPaint)
    }

    private fun buildPath() {
        if (data.isEmpty()) {
            return
        }

        val minX = data.minOf { it.x }
        val maxX = data.maxOf { it.x }
        val minY = data.minOf { it.y }
        val maxY = data.maxOf { it.y }

        val viewWidth = width - 2 * linePaint.strokeWidth
        val viewHeight = height - 2 * linePaint.strokeWidth

        val widthScale = viewWidth / (maxX - minX)
        val heightScale = viewHeight / (maxY - minY)

        val points = data.map { point ->
            val x = ((point.x - minX) * widthScale) + linePaint.strokeWidth
            val y = ((maxY - point.y) * heightScale) + linePaint.strokeWidth
            PointF(x, y)
        }.toMutableList()

        if (isStaking) {
            buildVerticalPaths(points, viewHeight)
            priceCoordinates.add(points.last().x - priceTextPaint.measureText(minMaxPrice.first()) - 16.dp to viewHeight - labelTextHeight - 4.dp - priceTextHeight)
            priceCoordinates.add(points.last().x - priceTextPaint.measureText(minMaxPrice.last()) - 16.dp to points.last().y + priceTextHeight)
        }

        if (square) {
            buildSquarePath(points, viewHeight)
        } else {
            buildDefaultPath(points, viewHeight)
        }

        gradientPaint.shader = LinearGradient(
            0f, 0f, 0f, viewHeight,
            accentColor.withAlpha(.3f), Color.TRANSPARENT, Shader.TileMode.CLAMP
        )
    }

    private fun buildVerticalPaths(points: MutableList<PointF>, viewHeight: Float) {
        verticalPaths = points.mapIndexed { index, pointF ->
            if (index % 3 == 0) {

                labelCoordinates.add(pointF.x + 2.dp to viewHeight - labelTextHeight)

                val verticalPath = Path()
                verticalPath.moveTo(pointF.x - linePaint.strokeWidth, pointF.y)
                verticalPath.lineTo(
                    pointF.x - linePaint.strokeWidth,
                    viewHeight - (linePaint.strokeWidth * 2)
                )
                verticalPath.close()
                verticalPath
            } else {
                Path()
            }
        }
    }

    private fun buildDefaultPath(points: MutableList<PointF>, viewHeight: Float) {
        path.reset()
        val firstPoint = points.removeFirst()
        val lastPoint = points.removeLast()

        path.moveTo(firstPoint.x - linePaint.strokeWidth, firstPoint.y)
        for (point in points) {
            path.lineTo(point.x, point.y)
        }

        path.lineTo(lastPoint.x + (linePaint.strokeWidth * 2f), lastPoint.y)
        path.lineTo(
            lastPoint.x + (linePaint.strokeWidth * 2f),
            viewHeight + (linePaint.strokeWidth * 2)
        )
        path.lineTo(0f, viewHeight + (linePaint.strokeWidth * 2))
        path.close()
    }

    private fun buildSquarePath(points: MutableList<PointF>, viewHeight: Float) {
        path.reset()

        val firstPoint = points.first()
        val lastPoint = points.last()
        path.moveTo(firstPoint.x - linePaint.strokeWidth, firstPoint.y)
        for ((index, point) in points.withIndex()) {
            if (index < points.size - 1) {
                val nextPoint = points[index + 1]
                path.lineTo(nextPoint.x, point.y)
                path.lineTo(nextPoint.x, nextPoint.y)
            } else {
                path.lineTo(point.x, point.y)
            }
        }

        path.lineTo(lastPoint.x + (linePaint.strokeWidth * 2f), lastPoint.y)
        path.lineTo(
            lastPoint.x + (linePaint.strokeWidth * 2f),
            viewHeight + (linePaint.strokeWidth * 2)
        )
        path.lineTo(0f, viewHeight + (linePaint.strokeWidth * 2))
        path.close()
    }
}