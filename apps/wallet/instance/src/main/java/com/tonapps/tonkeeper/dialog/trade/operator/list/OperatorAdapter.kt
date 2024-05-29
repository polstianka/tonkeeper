package com.tonapps.tonkeeper.dialog.trade.operator.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.dialog.trade.operator.OperatorItem

class OperatorAdapter(
    private val onClick: (item: OperatorListItem) -> Unit,
) : com.tonapps.uikit.list.BaseListAdapter() {
    companion object {
        fun buildMethodItems(list: List<OperatorItem>): List<OperatorListItem> {
            val items = mutableListOf<OperatorListItem>()
            for ((index, item) in list.withIndex()) {
                val position = com.tonapps.uikit.list.ListCell.getPosition(list.size, index)
                items.add(OperatorListItem(item, position))
            }
            return items
        }
    }

    override fun createHolder(
        parent: ViewGroup,
        viewType: Int,
    ): com.tonapps.uikit.list.BaseListHolder<out com.tonapps.uikit.list.BaseListItem> {
        return OperatorHolder(parent, onClick)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.setHasFixedSize(false)
    }
}
