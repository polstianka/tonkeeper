package com.tonapps.tonkeeper.fragment.trade.pick_currency

import android.util.Log
import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.core.emit
import com.tonapps.tonkeeper.fragment.trade.domain.GetAvailableCurrenciesCase
import com.tonapps.tonkeeper.ui.screen.settings.currency.list.CurrencyItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.localization.getNameResIdForCurrency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class PickCurrencyViewModel(
    getAvailableCurrenciesCase: GetAvailableCurrenciesCase
) : ViewModel() {

    private val _events = MutableSharedFlow<PickCurrencyEvent>()
    private val arg = MutableSharedFlow<PickCurrencyFragmentArgs>(replay = 1)
    private val availableCurrencies = arg.map {
        getAvailableCurrenciesCase.execute(it.paymentMethodId)
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

    fun provideArgs(pickCurrencyFragmentArgs: PickCurrencyFragmentArgs) {
        emit(arg, pickCurrencyFragmentArgs)
    }

    fun onCurrencyClicked(code: String) {
        Log.wtf("###", "onCurrencyClicked: $code")
    }
}