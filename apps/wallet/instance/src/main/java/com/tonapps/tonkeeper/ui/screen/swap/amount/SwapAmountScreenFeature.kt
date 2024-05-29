package com.tonapps.tonkeeper.ui.screen.swap.amount

import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.Coin
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.api.swap.StonfiSwapAsset
import com.tonapps.tonkeeper.api.swap.SwapRepository
import com.tonapps.tonkeeper.api.swap.SwapSimulateData
import com.tonapps.tonkeeper.ui.screen.swap.SwapData
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

class SwapAmountScreenFeature(
    private val tokenRepository: TokenRepository,
    private val ratesRepository: RatesRepository,
    private val swapRepository: SwapRepository,
    private val settingsRepository: SettingsRepository
) : UiFeature<SwapAmountScreenState, SwapAmountScreenEffect>(SwapAmountScreenState()) {

    private val currency: WalletCurrency
        get() = settingsRepository.currency

    private val currentToken: AccountTokenEntity?
        get() = uiState.value.selectedToken

    private val currentTokenRec: AccountTokenEntity?
        get() = uiState.value.selectedTokenRec

    private val currentTokenCode: String
        get() = uiState.value.selectedTokenCode

    private val currentTokenCodeRec: String
        get() = uiState.value.selectedTokenCodeRec

    val currentBalance: Float
        get() = currentToken?.balance?.value?.toFloat() ?: 0f

    val currentBalanceRec: Float
        get() = currentTokenRec?.balance?.value?.toFloat() ?: 0f

    val decimals: Int
        get() = currentToken?.decimals ?: 9

    val decimalsRec: Int
        get() = currentTokenRec?.decimals ?: 9

    private val queueScope = QueueScope(Dispatchers.IO)

    fun update(it: SwapData) {
        viewModelScope.launch {
            updateUiState { currentState ->
                currentState.copy(
                    initialLoading = true
                )
            }
            loadData(it.initialTo)
        }
    }

    private fun getCurrentTokenAddress(): String {
        return currentToken?.address ?: "TON"
    }

    private fun getCurrentTokenAddressRec(): String {
        return currentTokenRec?.address ?: "TON"
    }

    private suspend fun loadData(initialTo: String?) = withContext(Dispatchers.IO) {
        val wallet = App.walletManager.getWalletInfo()!!
        val accountId = wallet.accountId
        val tokens = tokenRepository.get(currency, accountId, wallet.testnet)
        val assets = swapRepository.getAssets(wallet.address).sortedByDescending { it.balance.toFloat() }
        assets.forEach { asset ->
            if (asset.balance.toFloat() > 0) {
                tokens.find { it.symbol == asset.symbol }?.let {
                    val rates = ratesRepository.getRates(currency, it.address)
                    val balanceInCurrency = rates.convert(it.address, Coin.toCoins(asset.balance.toLongOrNull() ?: 0L, asset.decimals))
                    asset.balanceInCurrency = CurrencyFormatter.format(currency.code, balanceInCurrency).toString()
                }
            }
        }

        updateUiState {
            it.copy(
                wallet = wallet,
                tokens = tokens
            )
        }

        val from = assets.find { it.symbol == currentTokenCode.ifEmpty { "TON" } }
        val to = if (initialTo == null) null else assets.find { it.symbol == currentTokenCode.ifEmpty { initialTo } }

        updateUiState {
            it.copy(
                wallet = wallet,
                tokens = tokens,
                selectedTokenAddress = WalletCurrency.TON.code,
                swapFrom = from,
                swapTo = to,
                initialLoading = false
            )
        }

        setValue(0f, "0")
    }

    fun flip() {
        updateUiState {
            it.copy(
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

    fun setFromTo(from: StonfiSwapAsset?, to: StonfiSwapAsset?, flip: Boolean = false) {
        updateUiState {
            it.copy(
                swapFrom = from,
                swapTo = to
            )
        }

        if (from != null) {
            if (flip) {
                setValue(uiState.value.amountRec, uiState.value.swapToAmount)
            } else {
                setValue(uiState.value.amount, uiState.value.swapFromAmount)
            }
        }
        if (to != null) {
            if (flip) {
                setValueRec(uiState.value.amount, uiState.value.swapFromAmount)
            } else {
                setValueRec(uiState.value.amountRec, uiState.value.swapToAmount)
            }
        }
    }

    private suspend fun updateValue(
        newValue: Float,
        newValue2: String,
        simulate: SwapSimulateData?,
        slippage: Float,
        expert: Boolean
    ) {
        val wallet = App.walletManager.getWalletInfo() ?: return
        val accountId = wallet.accountId
        val currentTokenAddress = getCurrentTokenAddress()

        val rates = ratesRepository.getRates(currency, currentTokenAddress)
        val balanceInCurrency = rates.convert(currentTokenAddress, currentBalance)

        val insufficientBalance = newValue > currentBalance
        val remaining = if (newValue > 0) {
            val value = currentBalance - newValue
            CurrencyFormatter.format(currentTokenCode, value)
        } else {
            ""
        }

        var swapRate = ""
        var recAmount = "0"
        var recAmountFloat = 0f
        if (simulate != null) {
            val pr = CurrencyFormatter.format(
                currentTokenCodeRec,
                simulate.swapRate.toFloatOrNull() ?: 0f
            )
            swapRate = "1 $currentTokenCode ≈ $pr"
            recAmountFloat = Coin.toCoins(simulate.askUnits.toLongOrNull() ?: 0, decimalsRec)
            recAmount = CurrencyFormatter.format("", recAmountFloat).toString().replace(",", "")
            updateValueRec(recAmountFloat, recAmount)
        }

        updateUiState { currentState ->
            currentState.copy(
                rate = CurrencyFormatter.formatFiat(currency.code, balanceInCurrency),
                insufficientBalance = insufficientBalance,
                remaining = remaining,
                canContinue = !insufficientBalance && currentBalance > 0 && newValue > 0 && simulate != null && (expert || slippage > (simulate.priceImpact.toFloatOrNull() ?: 0f)),
                maxActive = currentBalance == newValue,
                available = CurrencyFormatter.format(currentTokenCode, currentBalance),
                //availableRec = CurrencyFormatter.format(currentTokenCodeRec, currentBalanceRec),
                simulateData = simulate,
                loadingSimulation = false,
                swapRate = swapRate,
                amountRec = recAmountFloat,
                swapToAmount = recAmount,
                slippage = slippage,
                expertMode = expert,
            )
        }
    }

    fun setValue(value: Float, value2: String) {
        updateUiState { currentState ->
            currentState.copy(
                canContinue = false,
                amount = value,
                swapFromAmount = value2,
                loadingSimulation = currentState.swapFrom != null && currentState.swapTo != null
            )
        }

        queueScope.submit {
                var simulate: SwapSimulateData? = null
                val wAddress = App.walletManager.getWalletInfo()!!.address
                if (uiState.value.swapFrom != null && uiState.value.swapTo != null) {
                    if ((value2.ifEmpty { "0" }.toFloatOrNull() ?: 0f) > 0) {
                        simulate = swapRepository.simulate(
                            uiState.value.swapFrom!!, uiState.value.swapTo!!,
                            value2.ifEmpty { "0" },
                            swapRepository.getSlippage(wAddress).toString()
                        )
                    }
                }
                val slippgage = swapRepository.getSlippage(wAddress)
                val expert = swapRepository.getExpertMode(wAddress)
                updateValue(value, value2, simulate, slippgage, expert)
        }
    }

    fun setValueRec(value: Float, value2: String) {
        //updateUiState { currentState ->
        //    currentState.copy(
        //        canContinue = false
        //    )
        // }

        viewModelScope.launch {
            updateValueRec(value, value2)
        }
    }

    private suspend fun updateValueRec(newValue: Float, newValue2: String) {
        val wallet = App.walletManager.getWalletInfo() ?: return
        val accountId = wallet.accountId
        val currentTokenAddress = getCurrentTokenAddressRec()

        val rates = ratesRepository.getRates(currency, currentTokenAddress)
        val balanceInCurrency = rates.convert(currentTokenAddress, currentBalanceRec)

        val insufficientBalance = newValue > currentBalanceRec
        val remaining = if (newValue > 0) {
            val value = currentBalanceRec - newValue
            CurrencyFormatter.format(currentTokenCodeRec, value)
        } else {
            ""
        }

        updateUiState { currentState ->
            currentState.copy(
                rateRec = CurrencyFormatter.formatFiat(currency.code, balanceInCurrency),
                insufficientBalanceRec = insufficientBalance,
                availableRec = CurrencyFormatter.format(currentTokenCodeRec, currentBalanceRec),
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        queueScope.cancel()
    }
}