package com.tonapps.tonkeeper.fragment.swap

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.Coin
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.fragment.swap.assets.item.AssetItem
import com.tonapps.tonkeeper.fragment.swap.model.Simulate
import com.tonapps.tonkeeper.fragment.swap.model.Slippage
import com.tonapps.tonkeeper.fragment.swap.model.SwapButtonState
import com.tonapps.tonkeeper.fragment.swap.model.SwapConfirmArgs
import com.tonapps.tonkeeper.fragment.swap.model.SwapState
import com.tonapps.tonkeeper.fragment.swap.model.SwapTarget
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.account.legacy.WalletManager
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.stonfi.StonfiRepository
import com.tonapps.wallet.data.stonfi.entities.StonfiAsset
import com.tonapps.wallet.data.token.TokenRepository
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.text.DecimalFormat

class SwapViewModel(
    private val walletManager: WalletManager,
    private val tokenRepository: TokenRepository,
    private val stonfiRepository: StonfiRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val decimalFormat = DecimalFormat("#.##")

    private val _slippage = MutableStateFlow<Slippage>(Slippage.Slippage1Percent)
    val slippageFlow = _slippage.asStateFlow()

    private val _stateFlow = MutableStateFlow<SwapState?>(null)
    val stateFlow = _stateFlow.asStateFlow().filterNotNull()

    private val _stateLoadingFlow = MutableStateFlow(false)
    val stateLoadingFlow = _stateLoadingFlow.asStateFlow()

    private val _queryFlow = MutableStateFlow("")
    val queryFlow = _queryFlow.asStateFlow()

    private val _assetsFlow = MutableStateFlow<List<AssetItem.Item>?>(null)
    private val assetsFlow = _assetsFlow.asStateFlow().filterNotNull()

    val buttonStateFlow = combine(stateFlow, stateLoadingFlow) { state, isLoading ->
        if (state.send.value == 0.0f) {
            return@combine SwapButtonState.EnterAmount
        }
        if (state.receive == null) {
            return@combine SwapButtonState.ChooseToken
        }
        if (state.simulate == null && isLoading) {
            return@combine SwapButtonState.Loading
        }
        if (state.simulate != null) {
            return@combine SwapButtonState.Confirm
        }

        return@combine SwapButtonState.EnterAmount
    }

    val simulateFlow = _stateFlow.asStateFlow().filterNotNull().map {
        if (it.simulate != null) {
            val sendAsset = it.sendAsset
            val receiveAsset = it.receiveAsset ?: return@map null
            val askUnits =
                Coin.toCoins(it.simulate.askUnits.toLong(), receiveAsset.decimals).toString()
            val minAskUnits = Coin.toCoins(
                it.simulate.minAskUnits.toLong(), receiveAsset.decimals
            ).toString()
            val feeUnits = Coin.toCoins(it.simulate.feeUnits.toLong()).toString()

            val simulate = Simulate(
                swapRate = "1 ${sendAsset.symbol} ≈ ${it.simulate.swapRate} ${receiveAsset.symbol}",
                priceImpact = decimalFormat.format(it.simulate.priceImpact.toFloat()) + " %",
                askUnits = askUnits,
                minimumReceived = CurrencyFormatter.format(
                    receiveAsset.symbol,
                    minAskUnits.toFloat()
                ).toString(),
                liquidityProviderFee = CurrencyFormatter.format(
                    receiveAsset.symbol,
                    feeUnits.toFloat()
                ).toString(),
                blockchainFee = "0.11 - 0.17 TON",
                route = "${sendAsset.symbol} » ${receiveAsset.symbol}"
            )
            simulate
        } else {
            null
        }
    }

    private var changeValueJob: Job? = null
    private var simulateJob: Job? = null

    suspend fun getSwapArgs(): SwapConfirmArgs? {
        val state = _stateFlow.value ?: return null
        val receive = state.receive?: return null
        val simulateData = state.simulate?: return null
        val simulate = simulateFlow.firstOrNull()?: return null

        return SwapConfirmArgs(
            walletAddress = "",
            send = state.send,
            receive = receive,
            simulate = simulate,
            offerAmount = simulateData.offerUnits,
            minAskAmount = simulateData.minAskUnits)
    }

    fun getItemsFlow(target: SwapTarget): Flow<List<AssetItem>> {
        return combine(queryFlow, stateFlow, assetsFlow) { query, state, items ->
            createListItems(items, query, state, target)
        }
    }

    private fun createListItems(
        items: List<AssetItem.Item>,
        query: String,
        state: SwapState,
        target: SwapTarget
    ): ArrayList<AssetItem> {
        val list = ArrayList<AssetItem>()
        list.add(AssetItem.Label(Localization.swap_assets_suggested))
        list.add(AssetItem.Label(Localization.swap_assets_other))

        val send = state.send
        val pairs = state.pairs[send.address]


        val filteredItems = items.filter {
            if (target == SwapTarget.Receive) {
                if (send.isTon && it.symbol == send.symbol)  {
                    return@filter false
                }
                if (pairs != null && !pairs.contains(it.address)) {
                    return@filter false
                }
            }
            val searchResult = (it.symbol.contains(query, ignoreCase = true) || it.subtitle.contains(
                query,
                ignoreCase = true
            ))
            return@filter searchResult
        }

        filteredItems.forEachIndexed { i, item ->
            val position = when {
                (i == 0 && filteredItems.size > 1) -> {
                    ListCell.Position.FIRST
                }

                (i != 0 && i != filteredItems.lastIndex) -> {
                    ListCell.Position.MIDDLE
                }

                (i == filteredItems.lastIndex && filteredItems.size > 1) -> {
                    ListCell.Position.LAST
                }

                else -> {
                    ListCell.Position.SINGLE
                }
            }
            item.position = position
        }
        list.addAll(filteredItems)
        return list
    }

    private suspend fun updateSimulate() {
        val state = _stateFlow.value
        if (state?.receive == null || state.send.value == 0.0f) {
            simulateJob?.cancel()
            _stateLoadingFlow.value = false
            return
        }

        val unitsPrepared = Coin.prepareValue(state.send.value.toString())
        val unitsBd = BigDecimal(unitsPrepared)
        if (unitsBd <= BigDecimal.ZERO) {
            _stateFlow.value = state.copy(
                simulate = null
            )
            return
        }

        val sendAsset = state.sendAsset
        val unitsConverted = Coin.toNano(unitsBd.toFloat(), sendAsset.decimals).toString()
        simulateJob = viewModelScope.launch {
            _stateLoadingFlow.value = true

            withContext(Dispatchers.IO) {
                val simulate = try {
                    stonfiRepository.simulate(
                        offersAddress = state.send.address,
                        askAddress = state.receive.address,
                        units = unitsConverted,
                        slippageTolerance = _slippage.value.value.toString(),
                    )

                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
                _stateLoadingFlow.value = false
                _stateFlow.value = state.copy(
                    simulate = simulate
                )
            }
        }
    }

    fun resetState() {
        _slippage.value = Slippage.Slippage1Percent

        _stateFlow.value = _stateFlow.value?.let { state ->
            state.copy(
                send = getTokenState(state.mapAssets, state.mapTokens, SwapTarget.Send, TON_SYMBOL),
                receive = null
            )
        }
    }

    init {
        viewModelScope.launch {
            loadData()
        }
    }

    private suspend fun loadData() = withContext(Dispatchers.IO) {
        val wallet = walletManager.getWalletInfo()!!
        val accountId = wallet.accountId

        val mergedResponse = listOf(
            async { stonfiRepository.assets()},
            async { stonfiRepository.pairs() }
        )
        @Suppress("UNCHECKED_CAST")
        val assets = mergedResponse[0].await() as List<StonfiAsset>
        @Suppress("UNCHECKED_CAST")
        val pairs = mergedResponse[1].await() as Map<String, List<String>>

        val mapAssets = assets.associateBy { it.symbol }

        val tokens = tokenRepository.get(settingsRepository.currency, accountId, wallet.testnet)
        val mapTokens = tokens.associateBy { it.symbol }

        _stateFlow.value = SwapState(
            wallet = wallet,
            mapAssets = mapAssets,
            mapTokens = mapTokens,
            pairs = pairs,
            send = getTokenState(mapAssets, mapTokens, SwapTarget.Send, TON_SYMBOL),
            receive = null,
            simulate = null,
        )
        _assetsFlow.value = createItems(mapTokens, assets)
    }

    private fun createItems(
        mapTokens: Map<String, AccountTokenEntity>,
        assets: List<StonfiAsset>
    ): List<AssetItem.Item> {
        val currency = settingsRepository.currency
        return assets.map { asset ->
            val token = mapTokens[asset.symbol]

            val icon = token?.imageUri ?: asset.imageUrl?.toUri()

            val balanceFormat: CharSequence
            val balanceFiatFormat: CharSequence
            if (token != null && token.balance.value != 0.0f) {
                balanceFormat = CurrencyFormatter.format(value = token.balance.value)
                balanceFiatFormat = CurrencyFormatter.formatFiat(currency.code, token.fiat)
            } else {
                balanceFormat = "0"
                balanceFiatFormat = ""
            }

            AssetItem.Item(
                position = ListCell.Position.SINGLE,
                icon = icon,
                symbol = asset.symbol,
                subtitle = asset.displayName ?: "",
                balanceFormat = balanceFormat,
                balanceFiatFormat = balanceFiatFormat,
                byTon = asset.kind == StonfiAsset.StonfiAssetKind.Wton,
                address = asset.contractAddress
            )
        }.sortedByDescending { it.balanceFormat != "0" }
    }

    private fun getTokenState(
        mapAssets: Map<String, StonfiAsset>,
        mapTokens: Map<String, AccountTokenEntity>,
        target: SwapTarget,
        symbol: String
    ): SwapState.TokenState {
        val token = mapTokens[symbol]


        val asset = mapAssets[symbol]!!
        val imageUri = token?.imageUri ?: asset.imageUrl?.toUri() ?: Uri.EMPTY

        val balance = token?.balanceFormat(target) ?: "0"
        return SwapState.TokenState(
            balance = balance,
            symbol = symbol,
            imageUri = imageUri,
            value = 0f,
            address = asset.contractAddress
        )
    }

    fun onAssetSearch(query: String) {
        _queryFlow.value = query
    }

    fun onSlippageChange(slippage: Slippage) {
        _slippage.value = slippage
    }

    fun onChangeValue(value: Float) {
        changeValueScope {
            changeValue(value)
        }
    }

    private fun changeValueScope(block: suspend CoroutineScope.() -> Unit) {
        changeValueJob?.cancel()
        changeValueJob = viewModelScope.launch(block = block)
    }

    fun onClickSendMax() {
        changeValueScope {
            val state = _stateFlow.value ?: return@changeValueScope
            val symbol = state.send.symbol
            val balance = state.mapTokens[symbol]?.balance?.value ?: 0f
            changeValue(value = balance)
        }
    }

    private suspend fun changeValue(value: Float) {
        val state = _stateFlow.value ?: return
        val send = state.send.copy(
            value = value
        )
        _stateFlow.value = state.copy(
            send = send,
            receive = state.receive
        )
        updateSimulate()
    }

    fun onClickSwap() {

    }

    fun onSelectAsset(target: SwapTarget, symbol: String) {
        viewModelScope.launch {
            val state = _stateFlow.value ?: return@launch
            val newAsset = getTokenState(state.mapAssets, state.mapTokens, target, symbol)

            var from = state.send
            var to = state.receive

            when (target) {
                SwapTarget.Send -> {
                    if (from.symbol != newAsset.symbol) {
                        from = newAsset
                    }
                    if (to != null && from.symbol == to.symbol) {
                        to = null
                    }
                }

                SwapTarget.Receive -> {
                    if (to == null || to.symbol != newAsset.symbol) {
                        to = newAsset
                    }
                }
            }
            if (state.send.symbol != from.symbol || state.receive?.symbol != to?.symbol) {
                _stateFlow.value = state.copy(
                    send = from,
                    receive = to
                )
                updateSimulate()
            }

        }
    }

    companion object {
        private const val TAG = "SwapViewModel"
        private val SUGGESTED = listOf("USD₮", "ANON")
        private const val TON_SYMBOL = "TON"
    }

    fun AccountTokenEntity.balanceFormat(target: SwapTarget): CharSequence {
        return when (target) {
            SwapTarget.Send -> {
                CurrencyFormatter.format(symbol, balance.value)
            }

            SwapTarget.Receive -> {
                CurrencyFormatter.format(value = balance.value)
            }
        }
    }

    /*
            val insufficientBalance = newValue > currentBalance
        val remaining = if (newValue > 0) {
            val value = currentBalance - newValue
            CurrencyFormatter.format(currentTokenCode, value)
        } else {
            ""
        }
     */
}