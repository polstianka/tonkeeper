package uikit.drawable

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import uikit.base.BaseDrawable

class SpaceDrawable(private val size: Int) : BaseDrawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.TRANSPARENT }

    override fun draw(canvas: Canvas) {
        canvas.drawRect(bounds, paint)
    }

    override fun getIntrinsicWidth(): Int {
        return size
    }

    override fun getIntrinsicHeight(): Int {
        return size
    }

    override fun getMinimumHeight(): Int {
        return size
    }

    override fun getMinimumWidth(): Int {
        return size
    }

}