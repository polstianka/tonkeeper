package com.tonapps.tonkeeper.ui.screen.swap.screens

import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import com.tonapps.tonkeeper.ui.screen.wallet.list.Item
import com.tonapps.tonkeeper.ui.screen.wallet.list.holder.Holder
import com.tonapps.tonkeeperx.R

class ReceiveMoreItemHolder(
    parent: ViewGroup,
) :
    Holder<Item.ReceiveMoreItem>(parent, R.layout.receive_more_item_view) {

    private val icExclamationMark = findViewById<AppCompatImageView>(R.id.icExclamationMark)
    private val textItemName = findViewById<TextView>(R.id.textItemName)
    private val textValueItem = findViewById<TextView>(R.id.textValueItem)
    override fun onBind(item: Item.ReceiveMoreItem) {
        icExclamationMark.isVisible = item.icExclamationMarkText != null

        textItemName.text = item.textItemName
        textValueItem.text = item.textValueItem
    }

}