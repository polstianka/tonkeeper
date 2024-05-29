package uikit.drawable

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable

class OverrideBoundsDrawable(vararg drawables: Drawable) : LayerDrawable(drawables) {
    private var overrideBounds = Rect()
    private var needOverrideBounds = false

    private val ignoredBounds = Rect()
    private var haveIgnoredCalls = false

    private fun Rect.haveChanges(other: Rect): Boolean {
        return this.haveChanges(other.left, other.top, other.right, other.bottom)
    }

    private fun Rect.haveChanges(left: Int, top: Int, right: Int, bottom: Int): Boolean {
        return this.left != left || this.top != top || this.right != right || this.bottom != bottom
    }


    fun setUseOriginalBounds() = setOverrideBounds(null)

    fun setOverrideBounds(left: Int, top: Int, right: Int, bottom: Int) {
        overrideBounds.set(left, top, right, bottom)
        setOverrideBounds(overrideBounds)
    }

    fun setOverrideBounds(bounds: Rect?) {
        if (bounds != null) {
            overrideBounds.set(bounds)
            if (!needOverrideBounds) {
                val current = getBounds()
                if (!current.isEmpty) {
                    ignoredBounds.set(current)
                    haveIgnoredCalls = true
                }
                needOverrideBounds = true
            }
            if (bounds.haveChanges(getBounds())) {
                super.setBounds(overrideBounds.left, overrideBounds.top, overrideBounds.right, overrideBounds.bottom)
                invalidateSelf()
            }
        } else {
            overrideBounds.setEmpty()
            if (needOverrideBounds) {
                needOverrideBounds = false
                if (haveIgnoredCalls) {
                    haveIgnoredCalls = false
                    super.setBounds(ignoredBounds)
                    invalidateSelf()
                }
            }
        }
    }

    override fun setBounds(bounds: Rect) {
        if (needOverrideBounds) {
            ignoredBounds.set(bounds)
            haveIgnoredCalls = true
        } else {
            haveIgnoredCalls = false
            super.setBounds(bounds)
        }
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        if (needOverrideBounds) {
            ignoredBounds.set(left, top, right, bottom)
            haveIgnoredCalls = true
        } else {
            haveIgnoredCalls = false
            super.setBounds(left, top, right, bottom)
        }
    }
}