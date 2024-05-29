package com.tonapps.tonkeeper.ui.screen.buysell.amount

import androidx.lifecycle.viewModelScope
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.api.buysell.BuySellRepository
import com.tonapps.tonkeeper.api.buysell.BuySellType
import com.tonapps.tonkeeper.api.buysell.TradeType
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.TokenRepository
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import core.QueueScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uikit.mvi.UiFeature

class BuySellAmountScreenFeature(
    private val tokenRepository: TokenRepository,
    private val ratesRepository: RatesRepository,
    private val buySellRepository: BuySellRepository,
    private val settingsRepository: SettingsRepository
) : UiFeature<BuySellAmountScreenState, BuySellAmountScreenEffect>(BuySellAmountScreenState()) {

    private val currency: WalletCurrency
        get() = settingsRepository.currency

    private val currentToken: AccountTokenEntity?
        get() = uiState.value.selectedToken

    private val currentTokenCode: String
        get() = uiState.value.selectedTokenCode

    val currentBalance: Float
        get() = currentToken?.balance?.value?.toFloat() ?: 0f

    val decimals: Int
        get() = currentToken?.decimals ?: 9

    private val queueScope = QueueScope(Dispatchers.IO)

    init {
        viewModelScope.launch {
            loadData()
        }
    }

    private fun getCurrentTokenAddress(): String {
        return currentToken?.address ?: "TON"
    }

    private suspend fun loadData() = withContext(Dispatchers.IO) {
        val wallet = App.walletManager.getWalletInfo()!!
        val accountId = wallet.accountId
        val tokens = tokenRepository.get(currency, accountId, wallet.testnet)

        val types = buySellRepository.getTypes(TradeType.BUY)
        val selectedType = if (types.isNotEmpty()) types[0] else null

        updateUiState { state ->
            state.copy(
                wallet = wallet,
                tokens = tokens,
                selectedTokenAddress = WalletCurrency.TON.code,
                types = types.map { it.copy(selected = it.id == selectedType?.id) },
                selectedType = selectedType
            )
        }
    }

    fun setTradeType(type: TradeType) {

        val newTypes = buySellRepository.getTypes(type)
        var newSelectedType = uiState.value.selectedType
        if (newSelectedType != null && newTypes.find { it.id == uiState.value.selectedType?.id } == null) {
            newSelectedType = newTypes[0]
        }
        updateUiState { state ->
            state.copy(
                tradeType = type,
                types = buySellRepository.getTypes(type).map { it.copy(selected = it.id == newSelectedType?.id) },
                selectedType = newSelectedType
            )
        }

        viewModelScope.launch {
            updateValue(uiState.value.amount)
        }
    }

    fun selectType(type: BuySellType) {
        updateUiState { state ->
            state.copy(
                types = state.types.map { it.copy(selected = it.id == type.id) },
                selectedType = type
            )
        }
    }

    private suspend fun updateValue(newValue: Float) {
        val wallet = App.walletManager.getWalletInfo() ?: return
        val accountId = wallet.accountId
        val currentTokenAddress = getCurrentTokenAddress()

        val rates = ratesRepository.getRates(currency, currentTokenAddress)
        val balanceInCurrency = rates.convert(currentTokenAddress, newValue)

        val insufficientBalance = newValue > currentBalance
        val remaining = if (newValue > 0) {
            val value = currentBalance - newValue
            CurrencyFormatter.format(currentTokenCode, value)
        } else {
            ""
        }

        updateUiState { currentState ->
            currentState.copy(
                amount = newValue,
                rate = "${"%.2f".format(balanceInCurrency ).replace(',', '.')} ${currency.code}",
                insufficientBalance = uiState.value.tradeType == TradeType.SELL && insufficientBalance,
                canContinue = newValue >= uiState.value.minAmount &&
                        (uiState.value.tradeType == TradeType.BUY || (uiState.value.tradeType == TradeType.SELL && currentBalance >= newValue))
            )
        }
    }

    fun setCurrency(currency: WalletCurrency) {
        updateUiState { currentState ->
            currentState.copy(
                currency = currency
            )
        }

        viewModelScope.launch {
            updateValue(uiState.value.amount)
        }
    }

    fun setValue(value: Float) {
        updateUiState { currentState ->
            currentState.copy(
                canContinue = false
            )
        }

        viewModelScope.launch {
            updateValue(value)
        }
    }

    override fun onCleared() {
        super.onCleared()
        queueScope.cancel()
    }


}