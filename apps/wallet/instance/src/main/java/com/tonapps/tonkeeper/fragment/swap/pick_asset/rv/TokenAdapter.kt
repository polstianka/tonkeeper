package com.tonapps.tonkeeper.fragment.swap.pick_asset.rv

import android.view.ViewGroup
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

class TokenAdapter(
    private val onItemClicked: (TokenListItem) -> Unit
) : BaseListAdapter() {

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        return TokenHolder(parent, onItemClicked)
    }
}