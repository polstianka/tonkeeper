package com.tonapps.tonkeeper.ui.screen.buysell.currency

import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.api.buysell.BuySellOperator
import com.tonapps.tonkeeper.api.buysell.BuySellRepository
import com.tonapps.tonkeeper.ui.screen.buysell.BuySellData
import com.tonapps.tonkeeper.ui.screen.settings.currency.CurrencyViewModel
import com.tonapps.tonkeeper.ui.screen.settings.currency.list.Item
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.core.WalletCurrency
import core.QueueScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uikit.mvi.UiFeature

class BuySellCurrencyScreenFeature() : UiFeature<BuySellCurrencyScreenState, BuySellCurrencyScreenEffect>(BuySellCurrencyScreenState()) {

    private val queueScope = QueueScope(Dispatchers.IO)

    private suspend fun loadData() = withContext(Dispatchers.IO) {

        val currencies = WalletCurrency.ALL
        val items = mutableListOf<Item>()
        for ((index, currency) in currencies.withIndex()) {
            val item = Item(
                currency = currency,
                nameResId = CurrencyViewModel.getNameResIdForCurrency(currency),
                selected = currency == uiState.value.currency.code,
                position = ListCell.getPosition(currencies.size, index)
            )
            items.add(item)
        }

        updateUiState { state ->
            state.copy(
                currencyList = items,
            )
        }
    }

    fun setData(data: BuySellData) {
        updateUiState { currentState ->
            currentState.copy(
                currency = data.currency
            )
        }

        viewModelScope.launch {
            loadData()
        }
    }

    fun setCurrency(currency: String) {
        updateUiState { state ->
            state.copy(
                currency = WalletCurrency(currency),
            )
        }
        viewModelScope.launch {
            loadData()
        }
    }

    override fun onCleared() {
        super.onCleared()
        queueScope.cancel()
    }
}