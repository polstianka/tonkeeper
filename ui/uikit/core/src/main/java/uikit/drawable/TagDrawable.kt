package uikit.drawable

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import com.tonapps.uikit.color.backgroundContentTintColor
import com.tonapps.uikit.color.textSecondaryColor
import uikit.R
import uikit.base.BaseDrawable
import uikit.extensions.dp
import uikit.extensions.setTextAppearance

class TagDrawable(context: Context, val text: String): BaseDrawable() {

    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        setTextAppearance(context, R.style.TextAppearance_Body4CAPS)
        color = context.textSecondaryColor
        textAlign = Paint.Align.CENTER
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.backgroundContentTintColor
    }
    private val textWidth = textPaint.measureText(text)
    private val rect = RectF().apply {
        left = MARGIN_START
        top = 0f
        right = left + PADDING_HORIZONTAL + textWidth + PADDING_HORIZONTAL
        bottom = PADDING_TOP + textPaint.textSize + PADDING_BOTTOM
    }

    private val minWidth = rect.right.toInt()
    private val minHeight = rect.bottom.toInt()

    override fun draw(canvas: Canvas) {
        canvas.drawRoundRect(rect, ROUND_CORNER, ROUND_CORNER, backgroundPaint)
        canvas.drawText(this.text, rect.centerX(), rect.bottom, textPaint)
    }

    override fun getIntrinsicWidth(): Int {
        return minWidth
    }

    override fun getIntrinsicHeight(): Int {
        return minHeight
    }

    override fun getMinimumHeight(): Int {
        return minHeight
    }

    override fun getMinimumWidth(): Int {
        return minWidth
    }

    companion object {
        private val ROUND_CORNER = 6f.dp
        private val MARGIN_START = 6f.dp
        private val PADDING_HORIZONTAL = 5f.dp
        private val PADDING_TOP = 2.5f.dp
        private val PADDING_BOTTOM = 3f.dp
    }
}