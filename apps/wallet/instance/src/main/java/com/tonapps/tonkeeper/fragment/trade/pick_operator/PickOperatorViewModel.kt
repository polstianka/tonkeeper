package com.tonapps.tonkeeper.fragment.trade.pick_operator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.core.emit
import com.tonapps.tonkeeper.core.observeFlow
import com.tonapps.tonkeeper.fragment.trade.domain.GetAvailableCurrenciesCase
import com.tonapps.tonkeeper.fragment.trade.domain.GetDefaultCurrencyCase
import com.tonapps.tonkeeper.fragment.trade.domain.GetPaymentOperatorsCase
import com.tonapps.tonkeeper.fragment.trade.pick_currency.PickCurrencyResult
import com.tonapps.tonkeeper.fragment.trade.pick_operator.rv.model.PaymentOperatorListItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.localization.getNameResIdForCurrency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PickOperatorViewModel(
    getAvailableCurrenciesCase: GetAvailableCurrenciesCase,
    getDefaultCurrencyCase: GetDefaultCurrencyCase,
    getPaymentOperatorsCase: GetPaymentOperatorsCase
) : ViewModel() {

    private val args = MutableSharedFlow<PickOperatorFragmentArgs>(replay = 1)
    private val _events = MutableSharedFlow<PickOperatorEvents>()
    val events: Flow<PickOperatorEvents>
        get() = _events

    private val pickedCurrency = MutableStateFlow<String?>(null)
    private val availableCurrencies = args.map {
        getAvailableCurrenciesCase.execute(it.paymentMethodId)
    }
    private val defaultCurrency = args.map {
        getDefaultCurrencyCase.execute(it.paymentMethodId)
    }
    val currencyCode = combine(
        args,
        availableCurrencies,
        defaultCurrency,
        pickedCurrency
    ) { arg, available, default, picked ->
        if (picked != null) {
            picked
        } else {
            val availableCurrency = available.firstOrNull { it.code == arg.selectedCurrencyCode }
            availableCurrency?.code ?: default.code
        }
    }
    val currencyName = this.currencyCode.map { it.getNameResIdForCurrency() }
    private val paymentOperatorsDomain = combine(
        args,
        currencyCode
    ) { arg, currencyCode ->
        getPaymentOperatorsCase.execute(
            arg.country,
            arg.paymentMethodId,
            currencyCode
        )
    }
    private val _paymentOperators = MutableStateFlow(emptyList<PaymentOperatorListItem>())
    val paymentOperators: Flow<List<PaymentOperatorListItem>>
        get() = _paymentOperators.filter { it.isNotEmpty() }
    private val pickedPaymentOperator = paymentOperators.map { it.firstOrNull { it.isPicked } }

    init {
        observeFlow(paymentOperatorsDomain) { domainItems ->
            _paymentOperators.value = domainItems.mapIndexed { index, paymentOperator ->
                PaymentOperatorListItem(
                    id = paymentOperator.id,
                    iconUrl = paymentOperator.iconUrl,
                    title = paymentOperator.name,
                    rate = paymentOperator.rate,
                    isPicked = index == 0,
                    isBest = index == 0,
                    position = ListCell.getPosition(domainItems.size, index)
                )
            }
        }
    }

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
        val paymentMethodId = args.first().paymentMethodId
        val currencyCode = this@PickOperatorViewModel.currencyCode.first()
        val event = PickOperatorEvents.PickCurrency(
            paymentMethodId,
            currencyCode
        )
        _events.emit(event)
    }

    fun onCurrencyPicked(result: PickCurrencyResult) {
        pickedCurrency.value = result.currencyCode
    }

    fun onPaymentOperatorClicked(it: PaymentOperatorListItem) = viewModelScope.launch {
        val pickedItem = pickedPaymentOperator.first()
        if (pickedItem?.id == it.id) return@launch
        val state = _paymentOperators.value.toMutableList()
        val iterator = state.listIterator()
        while (iterator.hasNext()) {
            val current = iterator.next()
            val updated = current.copy(
                isPicked = it.id == current.id
            )
            iterator.set(updated)
        }
        _paymentOperators.value = state
    }
}