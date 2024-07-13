package com.tonapps.tonkeeper.ui.screen.stake.choose

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import kotlin.math.max

class FlowLayout : ViewGroup {

    private var lineHeightSpace = 0

    class LayoutParams(var horizontalSpacing: Int, var verticalSpacing: Int) : ViewGroup.LayoutParams(0, 0)

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        assert(MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED)
        val width = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        var height = MeasureSpec.getSize(heightMeasureSpec) - paddingTop - paddingBottom
        val count = childCount
        var lineHeightSpace1 = 0

        var xpos = paddingLeft
        var ypos = paddingTop
        val childHeightMeasureSpec =
            if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST)
            } else {
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            }

        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                val lp = child.layoutParams as LayoutParams
                child.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), childHeightMeasureSpec)
                val childw = child.measuredWidth
                lineHeightSpace1 = max(lineHeightSpace1.toDouble(), (child.measuredHeight + lp.verticalSpacing).toDouble()).toInt()
                if (xpos + childw > width) {
                    xpos = paddingLeft
                    ypos += lineHeightSpace1
                }
                xpos += childw + lp.horizontalSpacing
            }
        }
        this.lineHeightSpace = lineHeightSpace1

        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            height = ypos + lineHeightSpace1
        } else if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            if (ypos + lineHeightSpace1 < height) {
                height = ypos + lineHeightSpace1
            }
        }
        setMeasuredDimension(width, height)
    }

    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return LayoutParams(1, 1)
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams) = p is LayoutParams

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val count = childCount
        val width = r - l
        var xpos = paddingLeft
        var ypos = paddingTop

        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                val childw = child.measuredWidth
                val childh = child.measuredHeight
                val lp = child.layoutParams as LayoutParams
                if (xpos + childw > width) {
                    xpos = paddingLeft
                    ypos += lineHeightSpace
                }
                child.layout(xpos, ypos, xpos + childw, ypos + childh)
                xpos += childw + lp.horizontalSpacing
            }
        }
    }
}