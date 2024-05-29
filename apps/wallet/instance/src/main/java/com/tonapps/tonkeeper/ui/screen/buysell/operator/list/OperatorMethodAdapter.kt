package com.tonapps.tonkeeper.ui.screen.buysell.operator.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.core.fiat.models.FiatItem
import com.tonapps.wallet.data.rates.entity.OperatorBuyRateEntity

class OperatorMethodAdapter(
    private val onClick: (item: OperatorMethodItem) -> Unit
) : com.tonapps.uikit.list.BaseListAdapter() {

    companion object {
        fun buildMethodItems(
            list: List<FiatItem>,
            operatorRateslist: Map<String, OperatorBuyRateEntity>
        ): List<OperatorMethodItem> {
            val items = mutableListOf<OperatorMethodItem>()
            for ((index, item) in list.withIndex()) {
                val position = com.tonapps.uikit.list.ListCell.getPosition(list.size, index)
                val rate = operatorRateslist.get(item.id)
                items.add(OperatorMethodItem(item, rate, position))
            }
            return items
        }
    }

    fun submit(
        items: List<FiatItem>,
        operatorRateslist: Map<String, OperatorBuyRateEntity>, commitCallback: Runnable? = null
    ) {
        submitList(buildMethodItems(items, operatorRateslist), commitCallback)
    }

    override fun createHolder(
        parent: ViewGroup,
        viewType: Int
    ): com.tonapps.uikit.list.BaseListHolder<out com.tonapps.uikit.list.BaseListItem> {
        return OperatorMethodHolder(parent, onClick)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.setHasFixedSize(false)
    }
}