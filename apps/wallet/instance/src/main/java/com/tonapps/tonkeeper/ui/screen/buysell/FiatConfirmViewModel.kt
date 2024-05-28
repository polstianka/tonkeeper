package com.tonapps.tonkeeper.ui.screen.buysell

import androidx.lifecycle.ViewModel
import com.tonapps.blockchain.Coin
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class FiatConfirmViewModel(
    private val settings: SettingsRepository,
    private val ratesRepository: RatesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FiatConfirmUiState())
    val uiState: StateFlow<FiatConfirmUiState> = _uiState

    init {
        _uiState.update { it.copy(currencyCode = settings.currency.code) }
    }

    fun setAmount(amount: Float) {
        updateValue(amount)
    }

    fun getCurrencyCode(): String {
        return settings.currency.code
    }

    fun onYouPayChanged(s: String) {
        updateValue(Coin.prepareValue(s).toFloat())
    }

    fun onYouGetChanged(s: String) {

    }

    private fun updateValue(newValue: Float) {
        val currency = settings.currency
        val rates = ratesRepository.getRates(currency, "TON")
        val balanceInCurrency = rates.convert("TON", newValue)

        _uiState.update { currentState ->
            currentState.copy(
                youPayInput = newValue.toString(),
                youGetInput = balanceInCurrency.toString(),
                canContinue = newValue > 0,
            )
        }
    }
}

data class FiatConfirmUiState(
    val youGetInput: String = "0",
    val youPayInput: String = "0",
    val canContinue: Boolean = false,
    val currencyCode: String = ""
)