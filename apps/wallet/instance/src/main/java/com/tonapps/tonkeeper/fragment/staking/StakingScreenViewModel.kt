package com.tonapps.tonkeeper.fragment.staking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.Coin
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.network.NetworkMonitor
import com.tonapps.tonkeeper.api.chart.ChartEntity
import com.tonapps.tonkeeper.api.chart.ChartPeriod
import com.tonapps.tonkeeper.api.iconUri
import com.tonapps.tonkeeper.api.percentage
import com.tonapps.tonkeeper.helper.Coin2
import com.tonapps.tonkeeper.ui.adapter.Item
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.entity.BalanceStakeEntity
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.pools.StakingHistoryRepository
import com.tonapps.wallet.data.pools.StakingPoolsRepository
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.rates.entity.RatesEntity
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.TokenRepositoryV2
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import com.tonapps.wallet.localization.Localization
import io.tonapi.models.PoolImplementationType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.ceil
import kotlin.math.roundToInt

class StakingScreenViewModel(
    private val settingsRepository: SettingsRepository,
    private val poolsApyChartRepository: StakingHistoryRepository,
    private val poolsRepository: StakingPoolsRepository,
    private val tokensRepository: TokenRepositoryV2,
    private val walletRepository: WalletRepository,
    private val networkMonitor: NetworkMonitor,
    ratesRepository: RatesRepository,
) : ViewModel() {
    private val _selectedPeriodFlow = MutableStateFlow(ChartPeriod.month)
    private val _requestsFlow = MutableStateFlow<Request?>(null)

    private val _stateFlow = combine(
        poolsRepository.storageFlow,
        poolsApyChartRepository.storageFlow,
        ratesRepository.storageFlow,
        _requestsFlow.filterNotNull(),
    ) { poolsStorage, chartStorage, ratesStorage, request ->
        val wallet = request.wallet
        val poolAddress = request.poolAddress
        val tokens = request.tokens
        val rates = ratesStorage.get(RatesRepository.Request(request.currency))?.result

        val poolReq = StakingPoolsRepository.Request(wallet.accountId, wallet.testnet)
        val chartReq = StakingHistoryRepository.Request(poolAddress, wallet.testnet)

        val poolsS = poolsStorage.get(poolReq)
        val chartData =
            chartStorage.get(chartReq)?.result?.data?.map { ChartEntity(it.x, it.y.toFloat()) }
                ?: emptyList()

        val pools = poolsS?.result ?: return@combine null
        val pool = pools.pools[poolAddress] ?: return@combine null

        val entity =
            tokens.find { it.balance.stake?.pool?.address == poolAddress } ?: return@combine null

        State(
            wallet = wallet,
            entity = entity,
            stake = entity.balance.stake!!,
            chartData = chartData,
            isMaxApy = pool.address == pools.maxApyPool?.address,
            rates = rates, currency = request.currency
        )
    }.filterNotNull()

    val uiItemsFlow = combine(_stateFlow, _selectedPeriodFlow, this::buildItems)


    /* * */

    private var listener: ((s: BalanceStakeEntity) -> Unit)? = null

    fun load(l: (s: BalanceStakeEntity) -> Unit, poolAddress: String) {
        listener = l
        combine(
            tokensRepository.storageFlow,
            settingsRepository.currencyFlow,
            settingsRepository.languageFlow,
            walletRepository.activeWalletFlow,
            networkMonitor.isOnlineFlow,
        ) { tokens, currency, language, wallet, isOnline ->
            _requestsFlow.value = Request(
                tokens = tokens.assets(wallet.accountId, wallet.testnet, currency),
                wallet = wallet,
                language = language.code,
                poolAddress = poolAddress,
                currency = currency
            )

            if (!isOnline) {
                return@combine null
            }

            poolsApyChartRepository.doRequest(
                StakingHistoryRepository.Request(
                    poolAddress,
                    wallet.testnet
                )
            )
            poolsRepository.doRequest(
                StakingPoolsRepository.Request(
                    wallet.accountId,
                    wallet.testnet
                )
            )
        }.launchIn(viewModelScope)
    }


    /* * */

    data class State(
        val wallet: WalletEntity,
        val entity: AccountTokenEntity,
        val stake: BalanceStakeEntity,
        val chartData: List<ChartEntity>,
        val isMaxApy: Boolean,
        val currency: WalletCurrency,
        val rates: RatesEntity?
    )

    data class Request(
        val wallet: WalletEntity,
        val poolAddress: String,
        val language: String,
        val tokens: List<AccountTokenEntity>,
        val currency: WalletCurrency
    )


    /* * */

    private fun buildItems(state: State, period: ChartPeriod): List<BaseListItem> {
        val token = state.entity
        val pool = state.stake.pool
        val stake = state.stake

        val items = mutableListOf<BaseListItem>()

        val headerBalance = BigDecimal(state.stake.amountNano, TokenEntity.TON.decimals)
        val headerBalanceFormat = CurrencyFormatter.format(
            TokenEntity.TON.symbol,
            value = headerBalance,
            TokenEntity.TON.decimals
        )
        val headerFiat =
            state.rates?.convert(TokenEntity.TON.address, headerBalance.toFloat()) ?: 0f
        val headerFiatFormat = CurrencyFormatter.formatFiat(state.currency.code, headerFiat)

        items.add(
            Item.StakingPageHeader(
                balance = headerBalanceFormat,
                currencyBalance = headerFiatFormat,
                iconUri = state.stake.pool.implementation.type.iconUri
            )
        )


        items.add(
            Item.StakingPageActions(
                state.wallet.type,
                state.stake.pool.address,
                state.entity
            )
        )

        var actionsCount = 0
        if (stake.pendingWithdrawNano > BigInteger.ZERO) {
            actionsCount++
        }
        if (stake.pendingDepositNano > BigInteger.ZERO) {
            actionsCount++
        }
        if (stake.readyWithdrawNano > BigInteger.ZERO) {
            actionsCount++
        }

        if (actionsCount > 0) {
            var index = 0

            if (state.stake.readyWithdrawNano > BigInteger.ZERO) {
                val balance = Coin.toCoins(state.stake.readyWithdrawNano, TokenEntity.TON.decimals)
                val balanceFormat =
                    CurrencyFormatter.format(TokenEntity.TON.symbol, value = balance)
                val fiat = state.rates?.convert(TokenEntity.TON.address, balance) ?: 0f
                val fiatFormat = CurrencyFormatter.formatFiat(state.currency.code, fiat)

                items.add(
                    Item.StakingPagePendingAction(
                        action = Item.StakingPoolActionType.ReadyWithdraw,
                        position = ListCell.getPosition(actionsCount, index++),
                        testnet = false,
                        balance = balance,
                        balanceFormat = balanceFormat,
                        fiat = fiat,
                        fiatFormat = fiatFormat,
                        cycleEnd = pool.cycleEnd,
                        stake = stake,
                    )
                )
            }

            if (state.stake.pendingDepositNano > BigInteger.ZERO) {
                val balance = Coin.toCoins(state.stake.pendingDepositNano, TokenEntity.TON.decimals)
                val balanceFormat =
                    CurrencyFormatter.format(TokenEntity.TON.symbol, value = balance)
                val fiat = state.rates?.convert(TokenEntity.TON.address, balance) ?: 0f
                val fiatFormat = CurrencyFormatter.formatFiat(state.currency.code, fiat)

                items.add(
                    Item.StakingPagePendingAction(
                        action = Item.StakingPoolActionType.PendingDeposit,
                        position = ListCell.getPosition(actionsCount, index++),
                        testnet = false,
                        balance = balance,
                        balanceFormat = balanceFormat,
                        fiat = fiat,
                        fiatFormat = fiatFormat,
                        cycleEnd = pool.cycleEnd,
                        stake = stake
                    )
                )
            }

            if (state.stake.pendingWithdrawNano > BigInteger.ZERO) {
                val balance =
                    Coin.toCoins(state.stake.pendingWithdrawNano, TokenEntity.TON.decimals)
                val balanceFormat =
                    CurrencyFormatter.format(TokenEntity.TON.symbol, value = balance)
                val fiat = state.rates?.convert(TokenEntity.TON.address, balance) ?: 0f
                val fiatFormat = CurrencyFormatter.formatFiat(state.currency.code, fiat)

                items.add(
                    Item.StakingPagePendingAction(
                        action = Item.StakingPoolActionType.PendingWithdraw,
                        position = ListCell.getPosition(actionsCount, index++),
                        testnet = false,
                        balance = balance,
                        balanceFormat = balanceFormat,
                        fiat = fiat,
                        fiatFormat = fiatFormat,
                        cycleEnd = pool.cycleEnd,
                        stake = stake,
                    )
                )
            }

            items.add(Item.Offset8)
        }

        val chartData = state.chartData.toMutableList()
        if (chartData.isNotEmpty()) {
            chartData.add(
                ChartEntity(
                    x = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                    y = pool.apy.toFloat()
                )
            )
        }

        val chart = buildChart(chartData, period = period)
        if (chart.data.isNotEmpty()) {
            items.add(Item.StakingPageChartHeader(apy = state.stake.pool.apy.percentage))
            items.add(Item.ChartLegend(chart.data.maxBy { it.y }.y.percentage))
            items.add(chart)
            items.add(Item.ChartLegend(chart.data.minBy { it.y }.y.percentage))
            items.add(
                Item.StakingPageChartPeriod(
                    listener = _selectedPeriodFlow::value::set,
                    period = period
                )
            )
        }

        if (!token.isTon) {
            val balance = token.balance.value
            val balanceFormat = CurrencyFormatter.format(value = balance)
            val fiat = state.rates?.convert(token.address, balance) ?: 0f
            val fiatFormat = CurrencyFormatter.formatFiat(state.currency.code, fiat)

            items.add(
                Item.Token(
                    position = ListCell.Position.SINGLE,
                    iconUri = token.imageUri,
                    address = token.address,
                    symbol = token.symbol,
                    name = token.name,
                    balance = token.balance.value,
                    balanceFormat = balanceFormat,
                    fiat = token.fiat,
                    fiatFormat = fiatFormat,
                    rate = CurrencyFormatter.formatFiat(state.currency.code, token.rateNow),
                    rateDiff24h = token.rateDiff24h,
                    verified = token.verified,
                    testnet = state.wallet.testnet,
                    hiddenBalance = false,
                    mode = Item.TokenDisplayMode.Default,
                    onClickListener = null
                )
            )

            if (pool.implementation.type == PoolImplementationType.liquidTF) {
                items.add(Item.DescriptionBody3(Localization.staking_tonstakers_description))
            }
        }

        items.add(
            Item.StakingPagePoolInfoRows(
                apy = "≈ " + pool.apy.percentage,
                minimalDeposit = (Coin2.fromNano(pool.minStake)
                    .toString(TokenEntity.TON.decimals) + " " + TokenEntity.TON.symbol),
                isMaxApy = state.isMaxApy
            )
        )

        items.add(Item.DescriptionBody3(Localization.staking_warning))
        items.add(Item.TitleH3(Localization.staking_links))
        items.add(Item.Links(pool.links))

        return items
    }

    private fun buildChart(data: List<ChartEntity>, period: ChartPeriod): Item.StakingPageChart {
        val minTime = when (period) {
            ChartPeriod.month -> LocalDateTime.now().minusMonths(1)
            ChartPeriod.halfYear -> LocalDateTime.now().minusMonths(6)
            ChartPeriod.year -> LocalDateTime.now().minusYears(1)
            else -> LocalDateTime.MIN
        }.toEpochSecond(ZoneOffset.UTC)

        var result = data.filter { it.x >= minTime }
        val size = result.size

        if (size > 200) {
            val i = ceil(size / 200f).roundToInt()
            result = result.filterIndexed { index, _ -> (size - index - 1) % i == 0 }
        }

        return Item.StakingPageChart(
            period = period,
            data = result
        )
    }
}