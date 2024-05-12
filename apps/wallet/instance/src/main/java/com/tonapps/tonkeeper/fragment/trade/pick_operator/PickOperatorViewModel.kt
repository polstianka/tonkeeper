package com.tonapps.tonkeeper.fragment.trade.pick_operator

import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.core.emit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

class PickOperatorViewModel : ViewModel() {

    private val paymentMethodId = MutableStateFlow("")
    private val _events = MutableSharedFlow<PickOperatorEvents>()
    val events: Flow<PickOperatorEvents>
        get() = _events
    private val _subtitleText = MutableStateFlow("")
    private val _currencyCode = MutableStateFlow("AMD")
    val currencyCode: Flow<String>
        get() = _currencyCode
    private val _currencyName = MutableStateFlow("Armenian Dram")
    val currencyName: Flow<String>
        get() = _currencyName

    val subtitleText: Flow<String>
        get() = _subtitleText

    fun provideArguments(arguments: PickOperatorFragmentArgs) {
        _subtitleText.value = arguments.name
        paymentMethodId.value = arguments.id
        arguments.selectedCurrencyCode?.let { _currencyCode.value = it }
    }

    fun onChevronClicked() {
        emit(_events, PickOperatorEvents.NavigateBack)
    }

    fun onCrossClicked() {
        emit(_events, PickOperatorEvents.CloseFlow)
    }

    fun onCurrencyDropdownClicked() {
        emit(
            _events,
            PickOperatorEvents.PickCurrency(
                paymentMethodId.value,
                _currencyCode.value
            )
        )
    }
}