package com.tonapps.tonkeeper.ui.component.keyvalue

import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.tonapps.uikit.color.textPrimaryColor
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.uikit.list.BaseListHolder
import uikit.extensions.dp
import uikit.extensions.drawable
import uikit.extensions.setPaddingBottom
import uikit.extensions.setPaddingTop
import uikit.extensions.setPaddingVertical
import uikit.widget.ColumnLayout
import uikit.widget.RowLayout

class KeyValueSimpleRowHolder(
    private val view: RowLayout,
) : BaseListHolder<KeyValueModel.Simple>(view) {

    private val lpText = LinearLayoutCompat.LayoutParams(
        LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
        LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
        1.0f
    ).apply { gravity = Gravity.END }

    private val keyTextView = AppCompatTextView(context).apply {
        layoutParams = lpText
        setPaddingVertical(12.dp)
        setTextAppearance(uikit.R.style.TextAppearance_Body2)
        setTextColor(context.textSecondaryColor)
    }

    private val valueTextView = AppCompatTextView(context).apply {
        layoutParams = lpText
        setTextAppearance(uikit.R.style.TextAppearance_Label1)
    }

    private val captionTextView = AppCompatTextView(context).apply {
        layoutParams = lpText
        setTextAppearance(uikit.R.style.TextAppearance_Body2)
        setTextColor(context.textSecondaryColor)
        isVisible = false
    }

    init {
        val column = ColumnLayout(context).apply {
            layoutParams = lpText
        }
        column.addView(valueTextView)
        column.addView(captionTextView)
        view.addView(keyTextView)
        view.addView(column)
    }

    override fun onBind(item: KeyValueModel.Simple) {
        view.background = item.position.drawable(context)
        keyTextView.text = item.key
        valueTextView.text = item.value
        if (item.valueTint != null) {
            valueTextView.setTextColor(ContextCompat.getColor(context, item.valueTint))
        } else {
            valueTextView.setTextColor(context.textPrimaryColor)
        }
        if (item.caption != null) {
            valueTextView.setPaddingTop(12.dp)
            captionTextView.setPaddingBottom(12.dp)
            captionTextView.text = item.caption
            captionTextView.isVisible = true
        } else {
            valueTextView.setPaddingVertical(12.dp)
            captionTextView.isVisible = false
        }
    }
}