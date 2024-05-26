package com.tonapps.tonkeeper.fragment.jetton

import androidx.lifecycle.viewModelScope
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.api.jetton.JettonRepository
import com.tonapps.tonkeeper.api.parsedBalance
import com.tonapps.tonkeeper.api.withRetry
import com.tonapps.tonkeeper.core.history.HistoryHelper
import com.tonapps.tonkeeper.core.history.list.item.HistoryItem
import com.tonapps.wallet.api.Tonapi
import com.tonapps.wallet.data.account.legacy.WalletLegacy
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.RatesRepository
import core.QueueScope
import io.tonapi.models.AccountEvents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import uikit.mvi.AsyncState
import uikit.mvi.UiFeature

@Deprecated("Need refactoring")
class JettonScreenFeature(
    private val historyHelper: HistoryHelper,
    private val ratesRepository: RatesRepository,
): UiFeature<JettonScreenState, JettonScreenEffect>(JettonScreenState()) {

    private val jettonRepository = JettonRepository()
    private val accountsApi = Tonapi.accounts
    private val queueScope = QueueScope(viewModelScope.coroutineContext)

    private val currency: WalletCurrency
        get() = App.settings.currency

    fun loadMore(address: String, lt: Long) {
        queueScope.submit(Dispatchers.IO) {
            delay(1000)

            updateUiState { currentState ->
                currentState.copy(
                    historyItems = historyHelper.withLoadingItem(currentState.historyItems)
                )
            }

            val wallet = App.walletManager.getWalletInfo() ?: return@submit
            val events = getEvents(wallet, address, lt)

            val items = historyHelper.removeLoadingItem(uiState.value.historyItems) + events

            updateUiState { currentState ->
                currentState.copy(
                    walletType = wallet.type,
                    historyItems = items,
                    loadedAll = events.isEmpty()
                )
            }
        }
    }

    fun load(address: String) {
        queueScope.submit(Dispatchers.IO) {
            try {
                val wallet = App.walletManager.getWalletInfo() ?: throw IllegalStateException("Wallet not found")
                val accountId = wallet.accountId
                val jetton = jettonRepository.getByAddress(accountId, address, wallet.testnet) ?: throw IllegalStateException("Jetton not found")

                val balance = jetton.parsedBalance

                val rates = ratesRepository.getRates(currency, address)

                val currencyBalance = rates.convert(address, balance)
                val rate = rates.getRate(address)
                val rate24h = rates.getDiff24h(address)
                val historyItems = getEvents(wallet, address)

                updateUiState {
                    it.copy(
                        walletAddress = wallet.address,
                        walletType = wallet.type,
                        asyncState = AsyncState.Default,
                        jetton = jetton,
                        currencyBalance = CurrencyFormatter.formatFiat(currency.code, currencyBalance),
                        rateFormat = CurrencyFormatter.formatRate(currency.code, rate),
                        rate24h = rate24h,
                        historyItems = historyItems
                    )
                }

            } catch (ignored: Throwable) { }
        }
    }

    override fun onCleared() {
        super.onCleared()
        queueScope.cancel()
    }

    private suspend fun getEvents(
        wallet: WalletLegacy,
        jettonAddress: String,
        beforeLt: Long? = null
    ): List<HistoryItem> = withContext(Dispatchers.IO) {
        val accountId = wallet.accountId
        val events = getAccountEvent(accountId, wallet.testnet, jettonAddress, beforeLt) ?: return@withContext emptyList()
        historyHelper.mapping(wallet, events)
    }

    private suspend fun getAccountEvent(
        accountId: String,
        testnet: Boolean,
        jettonAddress: String,
        beforeLt: Long? = null
    ): AccountEvents? {
        return withRetry {
            accountsApi.get(testnet).getAccountJettonHistoryByID(
                accountId = accountId,
                jettonId = jettonAddress,
                beforeLt = beforeLt,
                limit = HistoryHelper.EVENT_LIMIT
            )
        }
    }
}