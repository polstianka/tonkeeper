package com.tonapps.tonkeeper.fragment.trade.buy.vm

import com.tonapps.tonkeeper.fragment.trade.domain.model.BuyMethod
import com.tonapps.tonkeeper.fragment.trade.ui.rv.mapper.BuyMethodMapper
import com.tonapps.tonkeeper.fragment.trade.ui.rv.model.TradeDividerListItem
import com.tonapps.tonkeeper.fragment.trade.ui.rv.model.TradeMethodListItem
import com.tonapps.uikit.list.BaseListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class BuyListHolder(
    private val mapper: BuyMethodMapper
) {

    private val _items = MutableStateFlow(emptyList<BaseListItem>())
    val items: Flow<List<BaseListItem>>
        get() = _items

    fun submitItems(domainItems: List<BuyMethod>) {
        val items = domainItems.map(mapper::map)
            .toMutableList<BaseListItem>()
        val iterator = items.listIterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next is TradeMethodListItem && iterator.hasNext()) {
                iterator.add(TradeDividerListItem)
            }
        }
        _items.value = items
    }
}