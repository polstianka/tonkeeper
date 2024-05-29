package com.tonapps.tonkeeper.ui.adapter.holder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.ui.adapter.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.BaseListHolder

class HolderTitleH3(
    parent: ViewGroup
) : BaseListHolder<Item.TitleH3>(parent, R.layout.holder_title_h3) {
    private val textView = findViewById<AppCompatTextView>(R.id.title)

    override fun onBind(item: Item.TitleH3) {
        if (item.text.isNullOrEmpty() && item.res != 0) {
            textView.setText(item.res)
        } else {
            textView.text = item.text
        }
    }
}