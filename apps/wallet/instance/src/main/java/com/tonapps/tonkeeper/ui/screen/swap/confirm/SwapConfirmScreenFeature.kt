package com.tonapps.tonkeeper.ui.screen.swap.confirm

import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.Coin
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.api.swap.StonfiSwapAsset
import com.tonapps.tonkeeper.api.swap.StonfiSwapHelper
import com.tonapps.tonkeeper.api.swap.SwapRepository
import com.tonapps.tonkeeper.ui.screen.swap.SwapData
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.TokenRepository
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import core.QueueScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uikit.mvi.UiFeature

class SwapConfirmScreenFeature(
    private val tokenRepository: TokenRepository,
    private val ratesRepository: RatesRepository,
    private val swapRepository: SwapRepository,
    private val settingsRepository: SettingsRepository
) : UiFeature<SwapConfirmScreenState, SwapConfirmScreenEffect>(SwapConfirmScreenState()) {

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

    init {
        viewModelScope.launch {
            loadData()
        }
    }

    private suspend fun loadData() = withContext(Dispatchers.IO) {
        val wallet = App.walletManager.getWalletInfo()!!
        val accountId = wallet.accountId
        val tokens = tokenRepository.get(currency, accountId, wallet.testnet)

        updateUiState {
            it.copy(
                wallet = wallet,
                tokens = tokens,
                selectedTokenAddress = WalletCurrency.TON.code
            )
        }
    }

    private fun getCurrentTokenAddress(): String {
        return currentToken?.address ?: "TON"
    }

    private fun getCurrentTokenAddressRec(): String {
        return currentTokenRec?.address ?: "TON"
    }

    fun swap(helper: StonfiSwapHelper) {
        updateUiState {
            it.copy(
                canContinue = false
            )
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val walletAddress = uiState.value.wallet?.address ?: ""
                val fromAddress = uiState.value.swapFrom?.contractAddress ?: ""
                val toAddress = uiState.value.swapTo?.contractAddress ?: ""
                val units = Coin.toNano(
                    uiState.value.swapFromAmount.toFloatOrNull() ?: 0f,
                    uiState.value.swapFrom?.decimals ?: 9
                ).toString()
                val fromTon = uiState.value.swapFrom?.kind == "Ton"
                val fromJetton = uiState.value.swapFrom?.kind == "Jetton"
                val toTon = uiState.value.swapTo?.kind == "Ton"
                val toJetton = uiState.value.swapTo?.kind == "Jetton"
                val minAsk = uiState.value.simulateData?.minAskUnits ?: "1"
                val wallet = App.walletManager.getWalletInfo()!!
                val accountId = wallet.accountId
                val tokens = tokenRepository.get(currency, accountId, wallet.testnet)
                when {
                    fromTon && toJetton -> {
                        withContext(Dispatchers.Main) {helper.tonToJetton(walletAddress, toAddress, units, minAsk) }
                    }
                    fromJetton && toTon -> {
                        withContext(Dispatchers.Main) { helper.jettonToTon(walletAddress, fromAddress, units, minAsk) }
                    }
                    fromJetton && toJetton -> {
                        val friendlyJettonAddress = tokens.find { it.symbol == uiState.value.swapTo?.symbol }?.address ?: toAddress
                        withContext(Dispatchers.Main) {
                            helper.jettonToJetton(
                                walletAddress,
                                fromAddress,
                                friendlyJettonAddress,
                                units,
                                minAsk
                            )
                        }
                    }
                }
                delay(1500)
                updateUiState {
                    it.copy(
                        canContinue = true
                    )
                }
            }
        }
    }

    fun setData(data: SwapData) {
        updateUiState { currentState ->
            currentState.copy(
                swapFromAmount = data.amountRaw,
                swapToAmount = data.amountToRaw,
                swapFrom = data.from,
                swapTo = data.to,
                simulateData = data.simulateData,
                slippage = data.slippage,
                expertMode = data.expertMode,
                canContinue = true
            )
        }

        val currentTokenAddress = getCurrentTokenAddress()
        val rates = ratesRepository.getRates(currency, currentTokenAddress)
        val balanceInCurrency = rates.convert(currentTokenAddress, data.amountRaw.toFloatOrNull() ?: 0f)
        val currentTokenAddressRec = getCurrentTokenAddressRec()
        val ratesRec = ratesRepository.getRates(currency, currentTokenAddressRec)
        val balanceInCurrencyRec = ratesRec.convert(currentTokenAddressRec, data.amountToRaw.toFloatOrNull() ?: 0f)

        updateUiState { currentState ->
            currentState.copy(
                available = if (balanceInCurrency > 0f) CurrencyFormatter.formatFiat(
                    currency.code,
                    balanceInCurrency
                ) else "",
                availableRec = if (balanceInCurrencyRec > 0f) CurrencyFormatter.formatFiat(
                    currency.code,
                    balanceInCurrencyRec
                ) else ""
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        queueScope.cancel()
    }
}