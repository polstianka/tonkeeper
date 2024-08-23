package com.tonapps.tonkeeper.ui.screen.battery.settings.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.ui.screen.battery.settings.list.holder.SettingsHeaderHolder
import com.tonapps.tonkeeper.ui.screen.battery.settings.list.holder.SupportedTransactionHolder
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

class Adapter: BaseListAdapter() {

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        return when(viewType) {
            Item.TYPE_SETTINGS_HEADER -> SettingsHeaderHolder(parent)
            Item.TYPE_SUPPORTED_TRANSACTION -> SupportedTransactionHolder(parent)
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.isNestedScrollingEnabled = true
    }
}