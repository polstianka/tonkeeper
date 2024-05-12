package com.tonapps.tonkeeper.fragment.trade.pick_operator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.core.emit
import com.tonapps.tonkeeper.fragment.trade.domain.GetAvailableCurrenciesCase
import com.tonapps.tonkeeper.fragment.trade.domain.GetDefaultCurrencyCase
import com.tonapps.wallet.localization.getNameResIdForCurrency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PickOperatorViewModel(
    getAvailableCurrenciesCase: GetAvailableCurrenciesCase,
    getDefaultCurrencyCase: GetDefaultCurrencyCase
) : ViewModel() {

    private val args = MutableSharedFlow<PickOperatorFragmentArgs>(replay = 1)
    private val _events = MutableSharedFlow<PickOperatorEvents>()
    val events: Flow<PickOperatorEvents>
        get() = _events

    private val availableCurrencies = args.map { getAvailableCurrenciesCase.execute(it.id) }
    private val defaultCurrency = args.map { getDefaultCurrencyCase.execute(it.id) }
    val currencyCode = combine(
        args,
        availableCurrencies,
        defaultCurrency
    ) { arg, available, default ->
            val availableCurrency = available.firstOrNull { it.code == arg.selectedCurrencyCode }
            availableCurrency?.code ?: default.code
        }
    val currencyName = this.currencyCode.map { it.getNameResIdForCurrency() }

    val subtitleText = args.map { it.name }

    fun provideArguments(arguments: PickOperatorFragmentArgs) {
        emit(args, arguments)
    }

    fun onChevronClicked() {
        emit(_events, PickOperatorEvents.NavigateBack)
    }

    fun onCrossClicked() {
        emit(_events, PickOperatorEvents.CloseFlow)
    }

    fun onCurrencyDropdownClicked() = viewModelScope.launch {
        val paymentMethodId = args.first().id
        val currencyCode = this@PickOperatorViewModel.currencyCode.first()
        val event = PickOperatorEvents.PickCurrency(
            paymentMethodId,
            currencyCode
        )
        _events.emit(event)
    }
}