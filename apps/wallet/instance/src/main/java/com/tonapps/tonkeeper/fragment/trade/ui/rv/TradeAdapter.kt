package com.tonapps.tonkeeper.fragment.trade.ui.rv

import android.view.ViewGroup
import com.tonapps.tonkeeper.fragment.trade.ui.rv.model.ExchangeMethodListItem
import com.tonapps.tonkeeper.fragment.trade.ui.rv.view.TradeMethodViewHolder
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

class TradeAdapter(
    private val onItemClicked: (ExchangeMethodListItem) -> Unit
) : BaseListAdapter() {

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        return TradeMethodViewHolder(parent, onItemClicked)
    }

    companion object {
        const val TYPE_TRADE_METHOD = 1
    }
}