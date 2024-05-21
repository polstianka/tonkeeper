package com.tonapps.tonkeeper.fragment.trade.exchange.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.core.emit
import com.tonapps.tonkeeper.core.observeFlow
import com.tonapps.tonkeeper.extensions.formattedRate
import com.tonapps.tonkeeper.fragment.trade.domain.GetExchangeMethodsCase
import com.tonapps.tonkeeper.fragment.trade.domain.GetRateFlowCase
import com.tonapps.tonkeeper.fragment.trade.exchange.ExchangeFragmentArgs
import com.tonapps.tonkeeper.fragment.trade.ui.rv.model.ExchangeMethodListItem
import com.tonapps.wallet.data.settings.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.math.BigDecimal

class ExchangeViewModel(
    getRateFlowCase: GetRateFlowCase,
    settingsRepository: SettingsRepository,
    getExchangeMethodsCase: GetExchangeMethodsCase,
    private val exchangeItems: ExchangeItems
) : ViewModel() {

    companion object {
        private const val TOKEN_TON = "TON"
    }
    private val args = MutableSharedFlow<ExchangeFragmentArgs>(replay = 1)
    private val country = settingsRepository.countryFlow
    private val currency = settingsRepository.currencyFlow
    private val methodsDomain = combine(country, args) { country, argument ->
        getExchangeMethodsCase.execute(country, argument.direction)
    }
    val methods = exchangeItems.items
    private val _events = MutableSharedFlow<ExchangeEvent>()
    val events: Flow<ExchangeEvent>
        get() = _events


    init {
        observeFlow(methodsDomain) { exchangeItems.submitItems(it) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val rate = currency.flatMapLatest { getRateFlowCase.execute(it) }

    private val amount = MutableStateFlow(BigDecimal.ZERO)

    val totalFiat = formattedRate(
        rateFlow = rate,
        amountFlow = amount,
        token = TOKEN_TON
    )
    val isButtonActive = combine(amount, exchangeItems.pickedItem) { currentAmount, _ ->
        currentAmount != BigDecimal.ZERO
    }

    fun onAmountChanged(newAmount: BigDecimal) {
        val oldAmount = this.amount.value
        if (oldAmount == newAmount) return

        this.amount.value = newAmount
    }

    fun onTradeMethodClicked(it: ExchangeMethodListItem) {
        exchangeItems.onMethodClicked(it.id)
    }

    fun onButtonClicked() = viewModelScope.launch {
        val paymentMethod = exchangeItems.pickedItem.first()
        val currency = currency.first()
        val direction = args.first().direction
        emit(
            _events,
            ExchangeEvent.NavigateToPickOperator(
                paymentMethodId = paymentMethod.id,
                paymentMethodName = paymentMethod.title,
                country = country.first(),
                currencyCode = currency.code,
                amount = amount.value,
                direction = direction
            )
        )
    }

    fun provideArgs(exchangeFragmentArgs: ExchangeFragmentArgs) = viewModelScope.launch {
        args.emit(exchangeFragmentArgs)
    }
}