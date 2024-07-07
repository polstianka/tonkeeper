package com.tonapps.tonkeeper.ui.component.keyvalue

import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.uikit.list.BaseListHolder
import uikit.extensions.dp
import uikit.extensions.setPaddingVertical
import uikit.widget.ColumnLayout
import uikit.widget.DividerView
import uikit.widget.LoaderView
import uikit.widget.RowLayout

class KeyValueHeaderRowHolder(private val view: ColumnLayout) :
    BaseListHolder<KeyValueModel.Header>(view) {

    private val lpText = LinearLayoutCompat.LayoutParams(
        LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
        LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
        1.0f
    )

    private val keyTextView = AppCompatTextView(context).apply {
        layoutParams = lpText
        textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        setPaddingVertical(8.dp)
        setTextAppearance(uikit.R.style.TextAppearance_Body2)
        setTextColor(context.textSecondaryColor)
    }

    private val loadingView = LoaderView(context).apply {
        layoutParams = LinearLayoutCompat.LayoutParams(16.dp, 16.dp).apply {
            gravity = Gravity.CENTER_VERTICAL or Gravity.END
        }
        startAnimation()
    }

    private val row = RowLayout(context).apply {
        layoutParams = LinearLayoutCompat.LayoutParams(16.dp, 16.dp)
    }

    init {
        row.addView(keyTextView)
        row.addView(loadingView)
        view.addView(DividerView(context))
        view.addView(row)
        view.addView(DividerView(context))
    }

    override fun onBind(item: KeyValueModel.Header) {
        keyTextView.text = item.key
        loadingView.isVisible = item.isLoading
    }
}