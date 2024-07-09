package com.tonapps.tonkeeper.ui.screen.swap.screens

import android.view.ViewGroup
import com.tonapps.tonkeeper.ui.screen.wallet.list.Item
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

class ReceiveMoreItemAdapter () : BaseListAdapter() {

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        return when (viewType) {
            Item.TYPE_RECEIVE_MORE_ITEM -> ReceiveMoreItemHolder(parent)
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

}