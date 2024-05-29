package com.tonapps.tonkeeper.ui.adapter.holder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.ui.adapter.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.BaseListHolder

class HolderTitleLabel1(
    parent: ViewGroup
) : BaseListHolder<Item.TitleLabel1>(parent, R.layout.holder_title_label1) {
    private val textView = findViewById<AppCompatTextView>(R.id.text)

    override fun onBind(item: Item.TitleLabel1) {
        if (item.text.isNullOrEmpty() && item.res != 0) {
            textView.setText(item.res)
        } else {
            textView.text = item.text
        }
    }
}