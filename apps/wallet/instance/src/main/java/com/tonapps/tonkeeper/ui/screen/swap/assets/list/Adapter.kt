package com.tonapps.tonkeeper.ui.screen.swap.assets.list

import android.view.ViewGroup
import com.tonapps.tonkeeper.ui.screen.swap.assets.AssetPickerScreen
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

class Adapter(val context: AssetPickerScreen) : BaseListAdapter() {
    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        return ViewHolder.valueOf(context, parent, ListItemType.fromOrdinal(viewType))
    }
}