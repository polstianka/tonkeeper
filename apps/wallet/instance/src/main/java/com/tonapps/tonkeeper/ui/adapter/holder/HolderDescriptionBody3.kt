package com.tonapps.tonkeeper.ui.adapter.holder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.ui.adapter.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.BaseListHolder

class HolderDescriptionBody3(
    parent: ViewGroup
) : BaseListHolder<Item.DescriptionBody3>(parent, R.layout.holder_description_body3) {
    private val textView = findViewById<AppCompatTextView>(R.id.text)

    override fun onBind(item: Item.DescriptionBody3) {
        if (item.text.isNullOrEmpty() && item.res != 0) {
            textView.setText(item.res)
        } else {
            textView.text = item.text
        }
    }
}