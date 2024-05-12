package com.tonapps.tonkeeper.fragment.trade.buy.vm

import androidx.lifecycle.ViewModel
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.core.emit
import com.tonapps.tonkeeper.core.observeFlow
import com.tonapps.tonkeeper.fragment.trade.domain.GetBuyMethodsCase
import com.tonapps.tonkeeper.fragment.trade.domain.GetRateFlowCase
import com.tonapps.tonkeeper.fragment.trade.ui.rv.model.TradeMethodListItem
import com.tonapps.wallet.data.settings.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class BuyViewModel(
    getRateFlowCase: GetRateFlowCase,
    settingsRepository: SettingsRepository,
    getBuyMethodsCase: GetBuyMethodsCase,
    private val buyListHolder: BuyListHolder
) : ViewModel() {

    companion object {
        private const val TOKEN_TON = "TON"
    }
    private val country = MutableStateFlow(settingsRepository.country)
    private val currency = settingsRepository.currencyFlow
    private val methodsDomain = country.map { getBuyMethodsCase.execute(it) }
    val methods = buyListHolder.items
    private val _events = MutableSharedFlow<BuyEvent>()
    val events: Flow<BuyEvent>
        get() = _events

    private var currencyValue = ""
    private var paymentMethodId = ""
    private var paymentMethodName = ""


    init {
        observeFlow(methodsDomain) { buyListHolder.submitItems(it) }
        observeFlow(currency) { currencyValue = it.code }
        observeFlow(buyListHolder.pickedItem) {
            paymentMethodId = it.id
            paymentMethodName = it.title
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val rate = currency.flatMapLatest { getRateFlowCase.execute(it) }
        .map { it.rate(TOKEN_TON) }
        .filterNotNull()
        .map { it.value }

    private val amount = MutableStateFlow(0f)

    val totalFiat = combine(amount, rate, currency) { amount, rate, currency ->
        val totalAmount = amount * rate
        CurrencyFormatter.format(currency.code, totalAmount)
    }
    val isButtonActive = combine(amount, buyListHolder.pickedItem) { currentAmount, _ ->
        !currentAmount.isNaN() && currentAmount != 0f
    }

    fun onAmountChanged(amount: String) {
        val oldAmount = this.amount.value
        val newAmount = amount.getValue()
        if (oldAmount == newAmount) return

        this.amount.value = newAmount
    }

    private fun String.getValue(): Float {
        return toFloatOrNull() ?: 0f
    }

    fun onTradeMethodClicked(it: TradeMethodListItem) {
        buyListHolder.onMethodClicked(it.id)
    }

    fun onButtonClicked() {
        emit(
            _events,
            BuyEvent.NavigateToPickOperator(
                paymentMethodId = paymentMethodId,
                paymentMethodName = paymentMethodName,
                country = country.value,
                currencyCode = currencyValue,
                amount = amount.value
            )
        )
    }
}