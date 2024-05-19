package com.tonapps.tonkeeper.core.history.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.core.history.list.holder.HistoryActionHolder
import com.tonapps.tonkeeper.core.history.list.holder.HistoryAppHolder
import com.tonapps.tonkeeper.core.history.list.holder.HistoryHeaderHolder
import com.tonapps.tonkeeper.core.history.list.holder.HistoryLoaderHolder
import com.tonapps.tonkeeper.core.history.list.item.HistoryItem
import com.tonapps.uikit.list.BaseListAdapter

open class HistoryAdapter(
    private val disableOpenAction: Boolean = false,
): BaseListAdapter() {

    init {
        super.setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        val item = super.getItem(position) as? HistoryItem ?: return RecyclerView.NO_ID
        return item.timestampForSort
    }

    override fun createHolder(parent: ViewGroup, viewType: Int): com.tonapps.uikit.list.BaseListHolder<out com.tonapps.uikit.list.BaseListItem> {
        return when (viewType) {
            HistoryItem.TYPE_ACTION -> HistoryActionHolder(parent, disableOpenAction)
            HistoryItem.TYPE_HEADER -> HistoryHeaderHolder(parent)
            HistoryItem.TYPE_LOADER -> HistoryLoaderHolder(parent)
            HistoryItem.TYPE_APP -> HistoryAppHolder(parent)
            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }

}