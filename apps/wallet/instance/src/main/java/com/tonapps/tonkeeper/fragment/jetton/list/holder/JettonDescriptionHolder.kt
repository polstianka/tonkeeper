package com.tonapps.tonkeeper.fragment.jetton.list.holder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.fragment.jetton.list.JettonItem
import com.tonapps.tonkeeperx.R

class JettonDescriptionHolder(
    parent: ViewGroup
) : JettonHolder<JettonItem.Description>(parent, R.layout.view_jetton_description) {

    override fun onBind(item: JettonItem.Description) {
        val textView = itemView as AppCompatTextView
        textView.text = item.text
    }
}