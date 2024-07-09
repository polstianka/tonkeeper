package com.tonapps.tonkeeper.fragment.trade.pick_currency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.core.emit
import com.tonapps.tonkeeper.fragment.trade.domain.GetAvailableCurrenciesCase
import com.tonapps.tonkeeper.ui.screen.settings.currency.list.CurrencyItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.localization.getNameResIdForCurrency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PickCurrencyViewModel(
    getAvailableCurrenciesCase: GetAvailableCurrenciesCase
) : ViewModel() {

    private val _events = MutableSharedFlow<PickCurrencyEvent>()
    private val arg = MutableSharedFlow<PickCurrencyFragmentArgs>(replay = 1)
    private val availableCurrencies = arg.map {
        getAvailableCurrenciesCase.execute(
            paymentMethodId = it.paymentMethodId,
            direction = it.direction
        )
    }

    val events: Flow<PickCurrencyEvent>
        get() = _events
    val items = combine(arg, availableCurrencies) { arg, list ->
        list.map {
            CurrencyItem(
                it.code,
                it.code.getNameResIdForCurrency(),
                it.code == arg.pickedCurrencyCode,
                ListCell.getPosition(list.size, list.indexOf(it))
            )
        }
    }
    private val pickedItem = items.map { it.firstOrNull { it.selected } }

    fun provideArgs(pickCurrencyFragmentArgs: PickCurrencyFragmentArgs) {
        emit(arg, pickCurrencyFragmentArgs)
    }

    fun onCurrencyClicked(code: String) = viewModelScope.launch {
        val picked = pickedItem.first()
        if (picked?.currency == code) return@launch
        emit(_events, PickCurrencyEvent.ReturnWithResult(code))
    }
}