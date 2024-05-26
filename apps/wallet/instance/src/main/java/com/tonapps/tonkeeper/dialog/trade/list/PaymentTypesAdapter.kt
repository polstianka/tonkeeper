package com.tonapps.tonkeeper.dialog.trade.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class PaymentTypesAdapter(
    private val onClick: (item: PaymentListItem) -> Unit,
) : com.tonapps.uikit.list.BaseListAdapter() {
    companion object {
        fun buildPaymentItems(list: List<PaymentItem>): List<PaymentListItem> {
            val items = mutableListOf<PaymentListItem>()
            for ((index, item) in list.withIndex()) {
                val position = com.tonapps.uikit.list.ListCell.getPosition(list.size, index)
                items.add(PaymentListItem(item, position))
            }
            return items
        }
    }

    fun submit(
        items: List<PaymentItem>,
        commitCallback: Runnable? = null,
    ) {
        submitList(buildPaymentItems(items), commitCallback)
    }

    override fun createHolder(
        parent: ViewGroup,
        viewType: Int,
    ): com.tonapps.uikit.list.BaseListHolder<out com.tonapps.uikit.list.BaseListItem> {
        return PaymentHolder(parent, onClick)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.setHasFixedSize(false)
    }
}
