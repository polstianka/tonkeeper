package uikit.drawable

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import com.tonapps.uikit.color.backgroundContentColor
import com.tonapps.uikit.color.backgroundHighlightedColor
import com.tonapps.uikit.color.separatorCommonColor
import com.tonapps.uikit.list.ListCell
import uikit.R
import uikit.base.BaseDrawable
import uikit.extensions.dp
import uikit.extensions.getDimension

class TopDividerDrawable(context: Context): BaseDrawable() {
    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.separatorCommonColor
    }

    private val dividerHeight = .5f.dp

    override fun draw(canvas: Canvas) {
        drawDivider(canvas, bounds)
    }

    private fun drawDivider(canvas: Canvas, bounds: Rect) {
        canvas.drawRect(
            bounds.left.toFloat(),
            bounds.top.toFloat(),
            bounds.right.toFloat(),
            bounds.top + dividerHeight,
            dividerPaint
        )
    }
}