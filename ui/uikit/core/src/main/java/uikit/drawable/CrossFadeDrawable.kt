package uikit.drawable

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable

class CrossFadeDrawable(
    private val first: Drawable,
    private val second: Drawable
) : LayerDrawable(arrayOf(first, second)) {

    init {
        second.alpha = 0
    }

    private var factor: Float = 0.0f

    fun setCrossFadeFactor(factor: Float) {
        if (this.factor != factor) {
            this.factor = factor
            invalidateSelf()
        }
    }


    override fun draw(canvas: Canvas) {
        first.alpha = (255.0f * (1f - factor)).toInt()
        second.alpha = (255.0f * factor).toInt()
        super.draw(canvas)
        first.alpha = 0xFF
        second.alpha = 0xFF
    }
}