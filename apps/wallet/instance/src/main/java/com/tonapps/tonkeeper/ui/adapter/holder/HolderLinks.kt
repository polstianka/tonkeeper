package com.tonapps.tonkeeper.ui.adapter.holder

import android.view.ViewGroup
import com.tonapps.tonkeeper.fragment.staking.deposit.view.LinksView
import com.tonapps.tonkeeper.ui.adapter.Item
import com.tonapps.uikit.list.BaseListHolder

class HolderLinks(
    parent: ViewGroup
) : BaseListHolder<Item.Links>(LinksView(parent.context)) {
    private val linksView = itemView as LinksView

    override fun onBind(item: Item.Links) {
        linksView.setLinks(item.links)
    }
}