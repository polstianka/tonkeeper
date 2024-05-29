package uikit.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.forEachIndexed
import uikit.effect.FadeAndScaleVisibilityEffect
import kotlin.math.max

class StretchyViewGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle) {
    private val childrenEffects: MutableList<FadeAndScaleVisibilityEffect> = mutableListOf()

    var animateDimensions: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                checkDimensions()
            }
        }

    fun setChildVisible(child: View, visible: Boolean, animated: () -> Boolean = { true }) {
        val index = childrenEffects.indexOfFirst { it.targets.contains(child) }
        if (index != -1) {
            setChildVisible(index, visible, animated)
        }
    }

    fun setChildVisible(index: Int, visible: Boolean, animated: () -> Boolean) =
        childrenEffects[index].setIsVisible(visible, animated)

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        childrenEffects += FadeAndScaleVisibilityEffect(child,
            initialValue = child.visibility == View.VISIBLE
        ) { _ ->
            checkDimensions()
        }
    }

    override fun onViewRemoved(child: View) {
        super.onViewRemoved(child)
        childrenEffects.removeAll {
            it.targets.contains(child)
        }
    }

    private fun checkDimensions() {
        val width = measuredWidth
        val height = measuredHeight
        var animatedWidth = 0
        var animatedHeight = 0
        this.forEachIndexed { index, child ->
            if (child.visibility != View.GONE) {
                val effect = childrenEffects[index]
                val childWidth = child.measuredWidth
                val childHeight = child.measuredHeight
                if (animateDimensions) {
                    animatedWidth = max(animatedWidth, (childWidth.toFloat() * effect.animatedValue).toInt())
                    animatedHeight = max(animatedHeight, (childHeight.toFloat() * effect.animatedValue).toInt())
                } else {
                    animatedWidth = max(animatedWidth, childWidth)
                    animatedHeight = max(animatedHeight, childHeight)
                }
            }
        }
        if (animatedWidth != width || animatedHeight != height) {
            requestLayout()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (!animateDimensions) return

        val width = measuredWidth
        val height = measuredHeight
        if (width == 0 && height == 0) return

        var animatedWidth = 0
        var animatedHeight = 0
        this.forEachIndexed { index, child ->
            if (child.visibility != View.GONE) {
                val effect = childrenEffects[index]
                val childWidth = child.measuredWidth
                val childHeight = child.measuredHeight
                animatedWidth = max(animatedWidth, (childWidth.toFloat() * effect.animatedValue).toInt())
                animatedHeight = max(animatedHeight, (childHeight.toFloat() * effect.animatedValue).toInt())
            }
        }
        if (animatedWidth != width || animatedHeight != height) {
            super.onMeasure(
                MeasureSpec.makeMeasureSpec(animatedWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(animatedHeight, MeasureSpec.EXACTLY)
            )
        }
    }
}