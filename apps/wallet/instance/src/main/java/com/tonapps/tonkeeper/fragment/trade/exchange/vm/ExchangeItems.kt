package com.tonapps.tonkeeper.fragment.trade.exchange.vm

import com.tonapps.tonkeeper.fragment.trade.domain.model.ExchangeMethod
import com.tonapps.tonkeeper.fragment.trade.ui.rv.mapper.ExchangeMethodMapper
import com.tonapps.tonkeeper.fragment.trade.ui.rv.model.ExchangeMethodListItem
import com.tonapps.uikit.list.BaseListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

class ExchangeItems(
    private val mapper: ExchangeMethodMapper
) {

    private val _items = MutableStateFlow(emptyList<ExchangeMethodListItem>())
    val items: Flow<List<BaseListItem>>
        get() = _items
    val pickedItem = items.map { it.filterIsInstance<ExchangeMethodListItem>() }
        .mapNotNull { it.firstOrNull { it.isChecked } }

    fun submitItems(domainItems: List<ExchangeMethod>) {
        val size = domainItems.size
        val items = domainItems.mapIndexed { index, buyMethod ->
            mapper.map(buyMethod, index, size)
        }.toMutableList()
        val iterator = items.listIterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (iterator.previousIndex() == 0) {
                iterator.set(next.copy(isChecked = true))
            }
        }
        _items.value = items
    }

    fun onMethodClicked(id: String) = mutateItems { state ->
        val iterator = state.listIterator()
        while (iterator.hasNext()) {
            val current = iterator.next()
            val updated = current.copy(isChecked = current.id == id)
            iterator.set(updated)
        }
    }

    private inline fun mutateItems(crossinline mutator: (MutableList<ExchangeMethodListItem>) -> Unit) {
        val state = _items.value.toMutableList()
        mutator(state)
        _items.value = state
    }
}