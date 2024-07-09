package com.tonapps.tonkeeper.fragment.send.amount

import androidx.lifecycle.viewModelScope
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.App
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.token.TokenRepository
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import core.QueueScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uikit.mvi.UiFeature
import java.math.BigDecimal

@Deprecated("Need refactoring")
class AmountScreenFeature(
    private val tokenRepository: TokenRepository,
    private val ratesRepository: RatesRepository
) : UiFeature<AmountScreenState, AmountScreenEffect>(AmountScreenState()) {

    private val currency: WalletCurrency
        get() = App.settings.currency

    private val currentToken: AccountTokenEntity?
        get() = uiState.value.selectedToken

    private val currentTokenCode: String
        get() = uiState.value.selectedTokenCode

    val currentBalance: BigDecimal
        get() = currentToken?.balance?.value ?: BigDecimal.ZERO

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

        updateUiState {
            it.copy(
                wallet = wallet,
                tokens = tokens,
                selectedTokenAddress = WalletCurrency.TON.code,
            )
        }
    }

    fun selectToken(tokenAddress: String) {
        updateUiState {
            it.copy(
                selectedTokenAddress = tokenAddress,
                canContinue = false,
            )
        }
    }

    fun selectToken(token: AccountTokenEntity) {
        selectToken(token.address)

        viewModelScope.launch {
            updateValue(uiState.value.amount)
        }
    }

    private suspend fun updateValue(newValue: BigDecimal) {
        val currentTokenAddress = getCurrentTokenAddress()

        val rates = ratesRepository.getRates(currency, currentTokenAddress)
        val balanceInCurrency = rates.convert(currentTokenAddress, currentBalance)

        val insufficientBalance = newValue > currentBalance
        val remaining = if (newValue > BigDecimal.ZERO) {
            val value = currentBalance - newValue
            CurrencyFormatter.format(currentTokenCode, value)
        } else {
            ""
        }

        updateUiState { currentState ->
            currentState.copy(
                rate = CurrencyFormatter.formatFiat(currency.code, balanceInCurrency),
                insufficientBalance = insufficientBalance,
                remaining = remaining,
                canContinue = !insufficientBalance && currentBalance > BigDecimal.ZERO && newValue > BigDecimal.ZERO,
                maxActive = currentBalance == newValue,
                available = CurrencyFormatter.format(currentTokenCode, currentBalance)
            )
        }
    }

    fun setValue(value: BigDecimal) {
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