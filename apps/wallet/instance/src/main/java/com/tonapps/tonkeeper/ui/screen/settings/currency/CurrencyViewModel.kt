package com.tonapps.tonkeeper.ui.screen.settings.currency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.ui.screen.settings.currency.list.CurrencyItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.localization.getNameResIdForCurrency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class CurrencyViewModel(
    private val settings: SettingsRepository
): ViewModel() {

    private val _uiItemsFlow = MutableStateFlow<List<CurrencyItem>>(emptyList())
    val uiItemsFlow = _uiItemsFlow.asStateFlow().filter { it.isNotEmpty() }

    init {
        settings.currencyFlow.map { buildUiItems(it) }.onEach {
            _uiItemsFlow.value = it
        }.launchIn(viewModelScope)
    }

    fun selectCurrency(currency: String) {
        settings.currency = WalletCurrency(currency)
    }

    private fun buildUiItems(selectedCurrency: WalletCurrency): List<CurrencyItem> {
        val currencies = WalletCurrency.ALL
        val items = mutableListOf<CurrencyItem>()
        for ((index, currency) in currencies.withIndex()) {
            val item = CurrencyItem(
                currency = currency,
                nameResId = currency.getNameResIdForCurrency(),
                selected = currency == selectedCurrency.code,
                position = ListCell.getPosition(currencies.size, index)
            )
            items.add(item)
        }
        return items
    }
}
