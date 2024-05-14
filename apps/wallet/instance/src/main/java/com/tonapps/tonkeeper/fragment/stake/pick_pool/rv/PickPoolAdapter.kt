package com.tonapps.tonkeeper.fragment.stake.pick_pool.rv

import android.view.ViewGroup
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

class PickPoolAdapter(val onItemClicked: (PickPoolListItem) -> Unit) : BaseListAdapter() {

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        return PickPoolHolder(parent, onItemClicked)
    }

}