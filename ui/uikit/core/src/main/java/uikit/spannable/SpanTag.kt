package uikit.spannable

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.FontMetricsInt
import android.graphics.Rect
import android.graphics.RectF
import android.text.TextPaint
import android.text.style.ReplacementSpan
import com.tonapps.uikit.color.backgroundContentTintColor
import com.tonapps.uikit.color.textSecondaryColor
import uikit.R
import uikit.extensions.dp
import uikit.extensions.setTextAppearance

class SpanTag(context: Context, val text: String) : ReplacementSpan() {

    private val textPaint = TextPaint(ANTI_ALIAS_FLAG).apply {
        setTextAppearance(context, R.style.TextAppearance_Body4CAPS)
        color = context.textSecondaryColor
        textAlign = Paint.Align.CENTER
    }

    private val backgroundPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = context.backgroundContentTintColor
    }

    private val rect = RectF()

    private val textWidth = textPaint.measureText(this.text)
    private val fontMetrics = textPaint.fontMetrics
    private val textRect = Rect().also {
        textPaint.getTextBounds(text, 0, text.length, it);
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        rect.left = x + MARGIN_START
        rect.right = rect.left + PADDING_HORIZONTAL + textWidth + PADDING_HORIZONTAL
        val centerY = (bottom + top) / 2f
        rect.top = centerY - textPaint.textSize / 2f - PADDING_TOP
        rect.bottom = centerY + textPaint.textSize / 2f + PADDING_BOTTOM

        canvas.drawRoundRect(rect, ROUND_CORNER, ROUND_CORNER, backgroundPaint)
        val textY = rect.height() / 2f + textRect.height() / 2f - textRect.bottom
        canvas.drawText(this.text, rect.centerX(), rect.top + textY, textPaint)
    }

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: FontMetricsInt?
    ): Int {
        return (MARGIN_START + PADDING_HORIZONTAL + textPaint.measureText(this.text) + PADDING_HORIZONTAL).toInt()
    }

    companion object {
        private val MARGIN_START = 6.dp
        private val PADDING_HORIZONTAL = 5f.dp
        private val PADDING_TOP = 2.5f.dp
        private val PADDING_BOTTOM = 3f.dp
        private val ROUND_CORNER = 6f.dp
    }
}