package com.tonapps.tonkeeper.ui.screen.buysell.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.network.NetworkMonitor
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.settings.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BuySellViewModel(
    private val networkMonitor: NetworkMonitor,
    private val walletRepository: WalletRepository,
    private val settingsRepository: SettingsRepository,
    private val ratesRepository: RatesRepository,
    private val api: API,
) : ViewModel() {

    private val _screenStateFlow =
        MutableStateFlow<BuySellScreenState>(BuySellScreenState.initState)
    val screenStateFlow: StateFlow<BuySellScreenState> = _screenStateFlow

    private val isBuyMode: Boolean
        get() = true

    private val currency: WalletCurrency
        get() = settingsRepository.currency

    var inputValue: Double = 0.0

    var isPaymentSelected = false

    init {

        viewModelScope.launch {
            settingsRepository.currencyFlow.collect { walletCurrency ->
                _screenStateFlow.update {
                    it.copy(currencyCode = walletCurrency.code)
                }
                updateValue(inputValue)
            }
        }

    }

    private fun getCurrentTokenAddress(): String {
        return "TON"
    }

    private suspend fun updateValue(newValue: Double) = withContext(Dispatchers.IO) {
        inputValue = newValue

        val currentTokenAddress = getCurrentTokenAddress()

        val rates = ratesRepository.getRates(currency, currentTokenAddress)
        val balanceInCurrency = rates.convert(currentTokenAddress, newValue)

        // for sell
//        val insufficientBalance = newValue > currentBalance
//        val remaining = if (newValue > 0) {
//            val value = currentBalance - newValue
//            CurrencyFormatter.format(currentTokenCode, value)
//        } else {
//            ""
//        }

        val rate = CurrencyFormatter.formatFiat(currency.code, balanceInCurrency)

        val continueState = if (newValue > 0) {
            if (isPaymentSelected) {
                BuySellScreenState.ContinueState.NEXT
            } else BuySellScreenState.ContinueState.SELECT_PAYMENT
        } else BuySellScreenState.ContinueState.ENTER_AMOUNT

        // for sell
        // val available = CurrencyFormatter.format(currentTokenCode, currentBalance)

        _screenStateFlow.update { currentState ->
            currentState.copy(
                rate = rate,
                insufficientBalance = false,
                // canContinue = !insufficientBalance && currentBalance > 0 && newValue > 0,
                continueState = continueState,
                //available = available
            )
        }
    }

    fun setValue(value: Double) {
        _screenStateFlow.update { currentState ->
            currentState.copy(
                continueState = BuySellScreenState.ContinueState.DISABLE
            )
        }

        viewModelScope.launch {
            updateValue(value)
        }
    }

    fun setPaymentMethodSelection() {
        isPaymentSelected = true
        viewModelScope.launch {
            updateValue(inputValue)
        }
    }

}