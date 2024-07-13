package com.tonapps.tonkeeper.ui.screen.stake.options

import android.view.ViewGroup
import com.tonapps.uikit.list.BaseListAdapter
import io.tonapi.models.PoolImplementationType
import io.tonapi.models.PoolInfo

open class StateOptionsAdapter(val listener: (PoolImplementationType?, PoolInfo?) -> Unit) : BaseListAdapter() {

    override fun createHolder(
        parent: ViewGroup,
        viewType: Int
    ): com.tonapps.uikit.list.BaseListHolder<out com.tonapps.uikit.list.BaseListItem> {
        return when (viewType) {
            OptionItem.TYPE_OPTION -> StakeOptionsItemHolder(parent, listener)
            OptionItem.TYPE_HEADER -> StakeOptionsHeaderHolder(parent)
            OptionItem.TYPE_OPTION_LIST -> StakeOptionsListHolder(parent, listener)
            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }

}