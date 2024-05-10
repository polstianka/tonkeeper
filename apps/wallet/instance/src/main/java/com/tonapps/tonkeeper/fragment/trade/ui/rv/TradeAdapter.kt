package com.tonapps.tonkeeper.fragment.trade.ui.rv

import android.view.ViewGroup
import com.tonapps.tonkeeper.fragment.trade.ui.rv.model.TradeMethodListItem
import com.tonapps.tonkeeper.fragment.trade.ui.rv.view.TradeDividerViewHolder
import com.tonapps.tonkeeper.fragment.trade.ui.rv.view.TradeMethodViewHolder
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

class TradeAdapter(
    private val onItemClicked: (TradeMethodListItem) -> Unit
) : BaseListAdapter() {

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        return when (viewType) {
            TYPE_TRADE_METHOD -> TradeMethodViewHolder(parent, onItemClicked)
            TYPE_DIVIDER -> TradeDividerViewHolder(parent)
            else -> TODO()
        }
    }

    companion object {
        const val TYPE_TRADE_METHOD = 1
        const val TYPE_DIVIDER = 2
    }
}