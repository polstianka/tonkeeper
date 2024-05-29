package com.tonapps.tonkeeper.ui.component

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import com.tonapps.tonkeeperx.R
import kotlin.math.min

class SwapAmountInputContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val rowView: View
    private val inputView: EditText

    init {
        inflate(context, R.layout.view_amount_swap_input, this)
        rowView = findViewById(R.id.value_wrapper)
        inputView = findViewById(R.id.input)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        measureChild(rowView, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), heightMeasureSpec)

        rowView.pivotY = rowView.measuredHeight / 2f
        rowView.pivotX = 0f
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        val innerWidth = rowView.measuredWidth

        val scale = min((measuredWidth.toFloat()) / innerWidth, 1f)

        rowView.translationX = (measuredWidth - innerWidth * scale)
        rowView.scaleX = scale
        rowView.scaleY = scale
    }
}