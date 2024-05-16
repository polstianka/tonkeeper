package com.tonapps.tonkeeper.fragment.stake.confirm.rv

import android.view.ViewGroup
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

class ConfirmStakeAdapter : BaseListAdapter(){

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        return ConfirmStakeHolder(parent)
    }
}