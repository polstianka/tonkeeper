package com.tonapps.tonkeeper.ui.screen.settings.theme.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.extensions.capitalized
import com.tonapps.uikit.icon.UIKitIcon
import com.tonapps.uikit.list.BaseListHolder
import uikit.widget.item.ItemIconView

class Holder(
    parent: ViewGroup,
    private val onClick: (item: Item) -> Unit
): BaseListHolder<Item>(ItemIconView(parent.context)) {

    private val itemIconView = itemView as ItemIconView

    init {
        itemIconView.layoutParams = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onBind(item: Item) {
        itemIconView.setOnClickListener { onClick(item) }
        itemIconView.position = item.position
        itemIconView.text = item.theme.key.capitalized
        itemIconView.iconRes = if (item.selected) {
            UIKitIcon.ic_done_16
        } else {
            0
        }
    }

}