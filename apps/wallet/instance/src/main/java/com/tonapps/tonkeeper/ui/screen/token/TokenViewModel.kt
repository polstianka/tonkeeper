package com.tonapps.tonkeeper.ui.screen.token

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.core.history.HistoryHelper
import com.tonapps.tonkeeper.core.history.list.item.HistoryItem
import com.tonapps.tonkeeper.helper.DateHelper
import com.tonapps.tonkeeper.ui.screen.token.list.Item
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.ChartEntity
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.account.WalletType
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.events.EventsRepository
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.TokenRepository
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import io.tonapi.models.AccountEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.toImmutableList
import uikit.extensions.collectFlow

class TokenViewModel(
    private val application: Application,
    private val tokenAddress: String,
    private val tokenRepository: TokenRepository,
    private val settingsRepository: SettingsRepository,
    private val walletRepository: WalletRepository,
    private val api: API,
    private val eventsRepository: EventsRepository,
    private val historyHelper: HistoryHelper,
): AndroidViewModel(application) {

    private val _tokenFlow = MutableStateFlow<TokenData?>(null)
    val tokenFlow = _tokenFlow.asStateFlow().filterNotNull()

    private val _uiItemsFlow = MutableStateFlow<List<Item>?>(null)
    val uiItemsFlow = _uiItemsFlow.asStateFlow().filterNotNull()

    private val _uiHistoryFlow = MutableStateFlow<List<HistoryItem>?>(null)
    val uiHistoryFlow = _uiHistoryFlow.asStateFlow().filterNotNull()

    private val _chartFlow = MutableStateFlow<List<ChartEntity>?>(null)
    private val chartFlow = _chartFlow.asStateFlow().filterNotNull()

    init {
        collectFlow(walletRepository.activeWalletFlow) { wallet ->
            val list = tokenRepository.get(settingsRepository.currency, wallet.accountId, wallet.testnet)
            val token = list.firstOrNull { it.address == tokenAddress } ?: return@collectFlow
            val data = TokenData(token, wallet)
            _tokenFlow.value = TokenData(token, wallet)
            buildItems(data, emptyList())
            load(data)
        }

        combine(tokenFlow, chartFlow) { token, chart ->
            buildItems(token, chart)
        }.launchIn(viewModelScope)
    }

    private suspend fun load(data: TokenData) = withContext(Dispatchers.IO) {
        if (data.verified) {
            async { loadHistory(data.address, data.wallet) }
            loadMonthChart(data.address)
        } else {
            loadHistory(data.address, data.wallet)
        }
    }

    fun loadMore() {
        if (isLoading()) {
            return
        }

        viewModelScope.launch {
            val lastLt = lastLt() ?: return@launch
            val data = _tokenFlow.value ?: return@launch
            val oldValues = _uiHistoryFlow.value ?: emptyList()
            _uiHistoryFlow.value = historyHelper.withLoadingItem(oldValues)
            loadHistory(data.address, data.wallet, lastLt)
        }
    }

    private fun buildItems(token: TokenData, charts: List<ChartEntity>) {
        val items = mutableListOf<Item>()
        items.add(Item.Balance(
            balance = CurrencyFormatter.format(token.symbol, token.balance),
            fiat = CurrencyFormatter.format(settingsRepository.currency.code, token.fiat),
            iconUri = token.iconUri,
        ))
        items.add(Item.Actions(
            swapUri = api.config.swapUri,
            swap = token.verified && (token.wallet.type == WalletType.Default || token.wallet.type == WalletType.Signer),
            send = token.wallet.hasPrivateKey || token.wallet.type == WalletType.Signer,
            walletAddress = token.walletAddress,
            tokenAddress = token.address,
            token = token.token.balance.token,
            walletType = token.wallet.type,
        ))
        if (token.verified) {
            items.add(Item.Price(
                fiatPrice = CurrencyFormatter.format(settingsRepository.currency.code, token.token.rateNow),
                rateDiff24h = token.token.rateDiff24h
            ))
            items.add(Item.Chart(
                data = charts,
                square = false
            ))
        }

        _uiItemsFlow.value = items
    }

    private suspend fun loadHistory(
        tokenAddress: String,
        wallet: WalletEntity,
        beforeLt: Long? = null
    ) = withContext(Dispatchers.IO) {
        val accountEvents = eventsRepository.loadForToken(tokenAddress, wallet.accountId, wallet.testnet, beforeLt) ?: return@withContext
        val walletEventItems = mapping(wallet, accountEvents.events)
        if (beforeLt == null) {
            setEvents(walletEventItems)
        } else {
            val oldValue = (_uiHistoryFlow.value ?: emptyList()).toImmutableList().filter { it !is HistoryItem.Loader }
            setEvents(oldValue + walletEventItems)
        }
    }

    private suspend fun mapping(
        wallet: WalletEntity,
        events: List<AccountEvent>
    ): List<HistoryItem> {
        return historyHelper.mapping(
            wallet = wallet,
            events = events,
            removeDate = false,
            hiddenBalances = settingsRepository.hiddenBalances
        )
    }

    private fun setEvents(
        items: List<HistoryItem>
    ) {
        val preparedItems = items.filter { it is HistoryItem.Event || it is HistoryItem.App }
            .sortedBy { it.timestampForSort }
            .reversed()

        val uiItems = mutableListOf<HistoryItem>()
        var currentDate: String? = null

        for (item in preparedItems) {
            val timestamp = item.timestampForSort
            val dateFormat = DateHelper.formatDate(application, timestamp)
            if (dateFormat != currentDate) {
                uiItems.add(HistoryItem.Header(dateFormat, item.timestampForSort))
                currentDate = dateFormat
            }
            uiItems.add(item)
        }

        _uiHistoryFlow.value = uiItems.toList()
    }

    private fun foundLastItem(): HistoryItem.Event? {
        return _uiHistoryFlow.value?.lastOrNull { it is HistoryItem.Event } as? HistoryItem.Event
    }

    private fun isLoading(): Boolean {
        return _uiHistoryFlow.value?.lastOrNull { it is HistoryItem.Loader } != null
    }

    private fun lastLt(): Long? {
        val item = foundLastItem() ?: return null
        if (item.lt > 0) {
            return item.lt
        }
        return null
    }

    private suspend fun loadChart(
        token: String,
        startDateSeconds: Long,
        endDateSeconds: Long,
        points: Int
    ) = withContext(Dispatchers.IO) {
        _chartFlow.value = api.loadChart(token, settingsRepository.currency.code, startDateSeconds, endDateSeconds, points)
    }

    private suspend fun loadHourChart(token: String) {
        val endDateSeconds = System.currentTimeMillis() / 1000
        val startDateSeconds = endDateSeconds - 60 * 60
        loadChart(token, startDateSeconds, endDateSeconds, 60)
    }

    private suspend fun loadDayChart(token: String) {
        val endDateSeconds = System.currentTimeMillis() / 1000
        val startDateSeconds = endDateSeconds - 60 * 60 * 24
        loadChart(token, startDateSeconds, endDateSeconds, 24)
    }

    private suspend fun loadWeekChart(token: String) {
        val endDateSeconds = System.currentTimeMillis() / 1000
        val startDateSeconds = endDateSeconds - 60 * 60 * 24 * 7
        loadChart(token, startDateSeconds, endDateSeconds, 7)
    }

    private suspend fun loadMonthChart(token: String) {
        val endDateSeconds = System.currentTimeMillis() / 1000
        val startDateSeconds = endDateSeconds - 60 * 60 * 24 * 30
        loadChart(token, startDateSeconds, endDateSeconds, 30)
    }

    private suspend fun load6MonthChart(token: String) {
        val endDateSeconds = System.currentTimeMillis() / 1000
        val startDateSeconds = endDateSeconds - 60 * 60 * 24 * 30 * 6
        loadChart(token, startDateSeconds, endDateSeconds, 6)
    }

    private suspend fun loadYearChart(token: String) {
        val endDateSeconds = System.currentTimeMillis() / 1000
        val startDateSeconds = endDateSeconds - 60 * 60 * 24 * 365
        loadChart(token, startDateSeconds, endDateSeconds, 12)
    }
}