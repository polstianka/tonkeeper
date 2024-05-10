package com.tonapps.tonkeeper.fragment.trade.buy.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.fragment.trade.domain.GetRateFlowCase
import com.tonapps.tonkeeper.fragment.trade.ui.rv.model.TradeDividerListItem
import com.tonapps.tonkeeper.fragment.trade.ui.rv.model.TradeMethodListItem
import com.tonapps.wallet.data.settings.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class BuyViewModel(
    getRateFlowCase: GetRateFlowCase,
    settingsRepository: SettingsRepository
) : ViewModel() {

    companion object {
        private const val TOKEN_TON = "TON"
    }
    private val country = MutableStateFlow(settingsRepository.country)
    private val currency = settingsRepository.currencyFlow
    val methods = country.map {
        listOf(
            TradeMethodListItem(
                id = "1",
                isChecked = true,
                title = "Hehe",
                iconUrl = ""
            ),
            TradeDividerListItem,
            TradeMethodListItem(
                id = "2",
                isChecked = false,
                title = "Haha",
                iconUrl = ""
            )
        )
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
        Log.wtf("###", "onItemClicked: $it")
    }
}