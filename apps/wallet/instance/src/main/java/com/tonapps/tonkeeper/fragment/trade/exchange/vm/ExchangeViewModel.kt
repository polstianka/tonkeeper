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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
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

    private val amount = MutableStateFlow(BigDecimal.ZERO)
    private val args = MutableSharedFlow<ExchangeFragmentArgs>(replay = 1)
    private val country = settingsRepository.countryFlow
        .shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)
    private val currency = settingsRepository.currencyFlow
        .shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)
    @OptIn(ExperimentalCoroutinesApi::class)
    private val rate = currency.flatMapLatest { getRateFlowCase.execute(it) }
    private val methodsDomain = combine(country, args) { country, argument ->
        getExchangeMethodsCase.execute(country, argument.direction)
    }.shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)
    private val _events = MutableSharedFlow<ExchangeEvent>()
    private val pickedItem = exchangeItems.pickedItem
        .shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

    val methods = exchangeItems.items.shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)
    val minAmount = pickedItem.map { it.method.minAmount }
    val totalFiat = formattedRate(
        rateFlow = rate,
        amountFlow = amount,
        token = TOKEN_TON
    )
    val isButtonActive = combine(amount, minAmount) { currentAmount, minAmount ->
        currentAmount >= minAmount
    }
    val events: Flow<ExchangeEvent>
        get() = _events

    init {
        observeFlow(methodsDomain) { exchangeItems.submitItems(it) }
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
        val paymentMethod = pickedItem.first()
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