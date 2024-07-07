package com.tonapps.tonkeeper.ui.screen.stake

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.Coin
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.api.chart.ChartEntity
import com.tonapps.tonkeeper.api.chart.ChartPeriod
import com.tonapps.tonkeeper.api.jetton.JettonRepository
import com.tonapps.tonkeeper.api.parsedBalance
import com.tonapps.tonkeeper.api.symbol
import com.tonapps.tonkeeper.api.withRetry
import com.tonapps.tonkeeper.core.history.HistoryHelper
import com.tonapps.tonkeeper.core.history.list.item.HistoryItem
import com.tonapps.tonkeeper.fragment.chart.list.ChartItem
import com.tonapps.tonkeeper.fragment.jetton.list.JettonItem
import com.tonapps.tonkeeper.helper.NumberFormatter
import com.tonapps.tonkeeper.ui.screen.stake.StakedJettonState.Companion.getChartItems
import com.tonapps.tonkeeper.ui.screen.stake.StakedJettonState.Companion.getItems
import com.tonapps.tonkeeper.ui.screen.stake.StakedJettonViewModel.Companion.FORECAST_TAB_ID
import com.tonapps.tonkeeper.ui.screen.stake.StakedJettonViewModel.Companion.HISTORY_TAB_ID
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.WalletType
import com.tonapps.wallet.data.account.legacy.WalletLegacy
import com.tonapps.wallet.data.account.legacy.WalletManager
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.stake.StakeRepository
import com.tonapps.wallet.data.token.TokenRepository
import com.tonapps.wallet.localization.Localization
import core.ResourceManager
import io.tonapi.models.AccountEvents
import io.tonapi.models.JettonBalance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uikit.mvi.AsyncState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.pow

class StakedJettonViewModel(
    private val historyHelper: HistoryHelper,
    private val ratesRepository: RatesRepository,
    private val walletManager: WalletManager,
    private val jettonRepository: JettonRepository,
    private val settingsRepository: SettingsRepository,
    private val stakeRepository: StakeRepository,
    private val resourceManager: ResourceManager,
    private val tokenRepository: TokenRepository,
    private val api: API
) : ViewModel() {

    private val _uiState = MutableStateFlow(StakedJettonState())
    val uiState: StateFlow<StakedJettonState> = _uiState

    override fun onCleared() {
        stakeRepository.clear()
    }

    fun load(address: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val wallet = walletManager.getWalletInfo() ?: error("Wallet not found")
            val jetton = jettonRepository.getByAddress(wallet.accountId, address, wallet.testnet)
                ?: error("Jetton not found")
            val currency = settingsRepository.currency
            val token = tokenRepository.get(currency, wallet.accountId, wallet.testnet)
                .first { it.address == address }

            val currencyBalance = ratesRepository
                .getRates(currency, address)
                .convert(address, jetton.parsedBalance)

            val historyItems = getEvents(wallet, address)

            val stakePoolsEntity = stakeRepository.get()
            val pools = stakePoolsEntity.pools
            val maxApy = pools.maxByOrNull { it.apy } ?: error("No pools")
            val pool = if (address.isEmpty()) maxApy else pools.first {
                it.address == address || it.liquidJettonMaster == address
            }
            val isMaxApy = address.isEmpty() || pool.address == maxApy.address
            val implementation =
                stakePoolsEntity.implementations.getValue(pool.implementation.value)
            val links = implementation.socials + implementation.url

            val formatFiat = CurrencyFormatter.formatFiat(currency.code, currencyBalance)
            val rateFormat = CurrencyFormatter.formatRate(currency.code, token.rateNow)

            _uiState.update {
                it.copy(
                    walletAddress = wallet.address,
                    poolAddress = pool.address,
                    walletType = wallet.type,
                    asyncState = AsyncState.Default,
                    jetton = jetton,
                    currencyBalance = formatFiat,
                    historyItems = historyItems,
                    isMaxApy = isMaxApy,
                    apyValue = pool.apy.toFloat(),
                    apy = resourceManager.getString(
                        Localization.apy_short_percent_placeholder,
                        NumberFormatter.format(pool.apy)
                    ),
                    minDeposit = "${NumberFormatter.format(Coin.toCoins(pool.minStake))} TON",
                    links = links
                )
            }
            val token1 = JettonItem.Token(
                iconUri = Uri.parse(jetton.jetton.image),
                symbol = jetton.symbol,
                name = jetton.jetton.name,
                balanceFormat = _uiState.value.balance,
                fiatFormat = formatFiat,
                rate = rateFormat,
                rateDiff24h = token.rateDiff24h
            )
            _uiState.update {
                val newState = it.copy(token = token1)
                newState.copy(
                    items = newState.getItems(resourceManager),
                    chartItems = newState.getChartItems()
                )
            }
        }
    }

    fun onTabClicked(id: Int) {
        _uiState.update {
            val copySelected = it.copy(selectedTab = id)
            it.copy(selectedTab = id, items = copySelected.getItems(resourceManager))
        }
    }

    private suspend fun getEvents(
        wallet: WalletLegacy,
        jettonAddress: String,
        beforeLt: Long? = null
    ): List<HistoryItem> = withContext(Dispatchers.IO) {
        val accountId = wallet.accountId
        val events = getAccountEvent(accountId, wallet.testnet, jettonAddress, beforeLt)
            ?: return@withContext emptyList()
        historyHelper.mapping(wallet, events)
    }

    private suspend fun getAccountEvent(
        accountId: String,
        testnet: Boolean,
        jettonAddress: String,
        beforeLt: Long? = null
    ): AccountEvents? {
        return withRetry {
            api.accounts(testnet).getAccountJettonHistoryByID(
                accountId = accountId,
                jettonId = jettonAddress,
                beforeLt = beforeLt,
                limit = HistoryHelper.EVENT_LIMIT
            )
        }
    }

    companion object {
        const val HISTORY_TAB_ID = 0
        const val FORECAST_TAB_ID = 1
    }
}

data class StakedJettonState(
    val walletAddress: String = "",
    val poolAddress: String = "",
    val walletType: WalletType = WalletType.Default,
    val asyncState: AsyncState = AsyncState.Loading,
    val jetton: JettonBalance? = null,
    val currencyBalance: CharSequence = "",
    val historyItems: List<HistoryItem> = emptyList(),
    val isMaxApy: Boolean = false,
    val apy: String = "",
    val apyValue: Float = 0f,
    val minDeposit: String = "",
    val links: List<String> = emptyList(),
    val items: List<BaseListItem> = emptyList(),
    val selectedTab: Int = FORECAST_TAB_ID,
    val token: JettonItem.Token? = null,
    val chartItems: List<ChartItem> = emptyList()
) {
    val balance: CharSequence
        get() {
            val jetton = jetton ?: return ""
            return CurrencyFormatter.format(
                jetton.jetton.symbol,
                jetton.parsedBalance,
                jetton.jetton.decimals
            )
        }

    companion object {

        private val sdf = SimpleDateFormat("MM/yy", Locale.getDefault())

        fun StakedJettonState.getChartItems(): List<ChartItem> {
            val jetton = jetton ?: return emptyList()
            val items = mutableListOf<ChartItem>()
            items.add(
                ChartItem.Header(
                    balance = balance,
                    currencyBalance = currencyBalance,
                    iconUrl = jetton.jetton.image,
                    staked = true
                )
            )
            items.add(ChartItem.ActionsStaked(walletAddress, jetton, walletType, poolAddress))
            items.add(ChartItem.Price(balance, token?.rateDiff24h.orEmpty()))
            items.add(
                ChartItem.Chart(
                    ChartPeriod.year,
                    getChart(),
                    getMonths(),
                    true,
                    getMinMaxFormattedValue()
                )
            )
            items.add(ChartItem.Divider)
            return items
        }

        fun StakedJettonState.getItems(
            resourceManager: ResourceManager
        ): List<JettonItem> {
            val items = mutableListOf<JettonItem>()
            items.add(JettonItem.Tabs(selectedTab == HISTORY_TAB_ID))
            if (selectedTab == FORECAST_TAB_ID) {
                token?.let { items.add(it) }
                items.add(JettonItem.Description(resourceManager.getString(Localization.staked_jetton_description)))
                items.add(
                    JettonItem.Details(
                        isApyMax = isMaxApy,
                        apy = apy,
                        minDeposit = minDeposit
                    )
                )
                items.add(JettonItem.Description(resourceManager.getString(Localization.pool_details_disclaimer)))
                items.add(JettonItem.Links(links))
            }
            return items
        }

        private fun StakedJettonState.getChart(): List<ChartEntity> {
            val coordinates = mutableListOf<ChartEntity>()
            val currentBalance = jetton?.parsedBalance ?: 0f
            val monthlyAPY = (1 + apyValue / 100).toDouble().pow(1.0 / 12) - 1
            for (month in 0 until 12) {
                val futurePrice = currentBalance * (1 + monthlyAPY).pow(month.toDouble())
                val timeStamp = getTimestampOfFutureMonth(month)
                coordinates.add(ChartEntity(timeStamp, futurePrice.toFloat()))
            }
            return coordinates
        }

        private fun getTimestampOfFutureMonth(monthsToAdd: Int): Long {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, monthsToAdd)
            return calendar.timeInMillis
        }

        private fun getMonths(): List<String> {
            val months = mutableListOf<String>()
            for (month in 0 until 12 step 3) {
                months.add(getMonthAndYear(month))
            }
            return months
        }

        private fun getMonthAndYear(monthsToAdd: Int): String {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, monthsToAdd)
            return sdf.format(calendar.time)
        }

        private fun StakedJettonState.getMinMaxFormattedValue(): List<String> {
            jetton ?: return emptyList()
            val currentBalance = jetton.parsedBalance
            val minFormatted = CurrencyFormatter.formatFiat(
                jetton.jetton.symbol,
                jetton.parsedBalance,
            ).toString()
            val monthlyAPY = (1 + apyValue / 100).toDouble().pow(1.0 / 12) - 1
            val maxBalance = currentBalance * (1 + monthlyAPY).pow(11.toDouble())
            val maxFormatted = CurrencyFormatter.formatFiat(
                jetton.jetton.symbol,
                maxBalance.toFloat(),
            ).toString()
            return listOf(minFormatted, maxFormatted)
        }
    }
}