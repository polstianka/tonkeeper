package com.tonapps.tonkeeper.fragment.stake.pick_option.rv

import android.view.ViewGroup
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

class StakingOptionAdapter(
    private val onItemClicked: (StakingOptionListItem) -> Unit
) : BaseListAdapter() {

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        return StakingOptionHolder(parent, onItemClicked)
    }
}