package com.tonapps.tonkeeper.ui.screen.stake.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.BOTTOM
import androidx.constraintlayout.widget.ConstraintSet.END
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.constraintlayout.widget.ConstraintSet.START
import androidx.constraintlayout.widget.ConstraintSet.TOP
import com.tonapps.uikit.color.textPrimaryColor
import com.tonapps.uikit.color.textSecondaryColor
import uikit.extensions.dp
import uikit.extensions.setPaddingVertical

class PoolDetailView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : ConstraintLayout(context, attrs, defStyle) {

    var titleTextView: AppCompatTextView
    var maxView: AppCompatTextView
    var valueTextView: AppCompatTextView

    init {
        setPaddingVertical(8.dp)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        titleTextView = AppCompatTextView(context).apply {
            id = View.generateViewId()
            setTextAppearance(uikit.R.style.TextAppearance_Body2)
            setTextColor(context.textSecondaryColor)
        }
        maxView = BadgeTextView(context).apply {
            id = View.generateViewId()
            text = "MAX"
        }
        valueTextView = AppCompatTextView(context).apply {
            id = View.generateViewId()
            setTextAppearance(uikit.R.style.TextAppearance_Body2)
            setTextColor(context.textPrimaryColor)
        }

        addView(titleTextView)
        addView(maxView)
        addView(valueTextView)

        val titleId = titleTextView.id
        val maxId = maxView.id
        val valueId = valueTextView.id
        val set = ConstraintSet()
        set.clone(this)
        set.connect(titleId, START, PARENT_ID, START, 16.dp)
        set.connect(titleId, TOP, PARENT_ID, TOP)

        set.connect(maxId, START, titleId, END, 6.dp)
        set.connect(maxId, TOP, titleId, TOP)

        set.connect(valueId, END, PARENT_ID, END, 16.dp)
        set.connect(valueId, TOP, titleId, TOP)
        set.connect(valueId, BOTTOM, titleId, BOTTOM)

        set.applyTo(this)
    }

}