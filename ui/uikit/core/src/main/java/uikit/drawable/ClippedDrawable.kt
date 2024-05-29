package uikit.drawable

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.LayerDrawable
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources

class ClippedDrawable(context: Context, @DrawableRes resId: Int) : LayerDrawable(arrayOf(AppCompatResources.getDrawable(context, resId))) {
    override fun draw(c: Canvas) {
        val bounds = bounds
        val count = c.save()
        c.clipRect(bounds)
        super.draw(c)
        c.restoreToCount(count)
    }
}