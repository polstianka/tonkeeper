package uikit.drawable

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable
import uikit.extensions.withAlpha

class RoundRectDrawable : Drawable() {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    var cornerRadius: Float = 0.0f
        set(value) {
            if (field == value) return
            field = value
            invalidateSelf()
        }
    var alpha: Float = 1.0f
        set(value) {
            if (field == value) return
            field = value
            invalidateSelf()
        }
    var color: Int = Color.TRANSPARENT
        set(value) {
            if (field == value) return
            field = value
            invalidateSelf()
        }

    private val rectF = RectF()

    override fun draw(c: Canvas) {
        rectF.set(bounds)
        val color = this.color.withAlpha(alpha)
        paint.color = color
        if (cornerRadius > 0.0f) {
            c.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)
        } else {
            c.drawRect(rectF, paint)
        }
    }

    override fun setAlpha(alpha: Int) {
        this.alpha = alpha.toFloat() / 255.0f
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.setColorFilter(colorFilter)
    }

    override fun getOpacity(): Int {
        return PixelFormat.UNKNOWN
    }
}