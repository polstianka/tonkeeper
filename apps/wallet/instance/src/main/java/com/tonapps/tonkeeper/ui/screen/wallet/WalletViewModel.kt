package com.tonapps.tonkeeper.ui.screen.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.network.NetworkMonitor
import com.tonapps.tonkeeper.ui.screen.wallet.list.Item
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.account.entities.WalletEvent
import com.tonapps.wallet.data.core.ScreenCacheSource
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.push.PushManager
import com.tonapps.wallet.data.push.entities.AppPushEntity
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.TokenRepositoryV2
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import com.tonapps.wallet.data.tonconnect.TonConnectRepository
import com.tonapps.wallet.data.tonconnect.entities.DAppEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import uikit.extensions.collectFlow

class WalletViewModel(
    private val walletRepository: WalletRepository,
    private val settings: SettingsRepository,
    private val tokenRepositoryV2: TokenRepositoryV2,
    private val api: API,
    private val networkMonitor: NetworkMonitor,
    private val pushManager: PushManager,
    private val tonConnectRepository: TonConnectRepository,
    private val screenCacheSource: ScreenCacheSource
): ViewModel() {

    private data class Params(
        val wallet: WalletEntity,
        val currency: WalletCurrency,
        val isOnline: Boolean,
        val push: List<AppPushEntity>,
        val apps: List<DAppEntity>,
    )

    private data class Tokens(
        val params: Params,
        val list: List<AccountTokenEntity>
    ) {
        val wallet get() = params.wallet
        val currency get() = params.currency
        val isOnline get() = params.isOnline
        val push get() = params.push
        val apps get() = params.apps
    }

    private val _paramsFlow = MutableStateFlow<Params?>(null)
    private val tokensFlow = combine(_paramsFlow.asStateFlow().filterNotNull(), tokenRepositoryV2.storageFlowIgnoreRates) { params, assetsStorage ->
        Tokens(params, assetsStorage.assets(params.wallet.accountId, params.wallet.testnet, params.currency))
    }.filterNotNull().filter { it.list.isNotEmpty() }

    private val _lastLtFlow = MutableStateFlow<Long>(0)
    private val lastLtFlow = _lastLtFlow.asStateFlow()

    private val _statusFlow = MutableSharedFlow<Item.Status>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val statusFlow = _statusFlow.asSharedFlow()

    private val _uiItemsFlow = MutableStateFlow<List<Item>>(emptyList())
    val uiItemsFlow = _uiItemsFlow.asStateFlow().filter { it.isNotEmpty() }

    val uiLabelFlow = walletRepository.activeWalletFlow.map { it.label }

    init {
        walletRepository.activeWalletFlow.map { screenCacheSource.getWalletScreen(it) }.flowOn(Dispatchers.IO).onEach { items ->
            if (items.isNullOrEmpty()) {
                _uiItemsFlow.value = listOf(Item.Skeleton(true))
            } else {
                _uiItemsFlow.value = items
            }
        }.launchIn(viewModelScope)

        collectFlow(settings.hiddenBalancesFlow.drop(1)) { hiddenBalance ->
            val wallet = walletRepository.activeWalletFlow.firstOrNull() ?: return@collectFlow
            val items = _uiItemsFlow.value.map {
                when (it) {
                    is Item.Balance -> it.copy(hiddenBalance = hiddenBalance)
                    is Item.Token -> it.copy(hiddenBalance = hiddenBalance)
                    else -> it
                }
            }
            _uiItemsFlow.value = items
            setCached(wallet, items)
        }

        collectFlow(walletRepository.realtimeEventsFlow) { event ->
            if (event is WalletEvent.Boc) {
                setStatus(Item.Status.SendingTransaction)
            } else if (event is WalletEvent.Transaction) {
                setStatus(Item.Status.TransactionConfirmed)
                delay(2000)
                setStatus(Item.Status.Default)
                _lastLtFlow.value = event.lt
            }
        }

        combine(
            walletRepository.activeWalletFlow,
            settings.currencyFlow,
            networkMonitor.isOnlineFlow,
            lastLtFlow,
            pushManager.dAppPushFlow,
        ) { wallet, currency, isOnline, lastLt, push ->
            unsubscribe()

            val pushes = push?.distinctBy { it.dappUrl } ?: emptyList()

            if (lastLt == 0L) {
                setStatus(Item.Status.Updating)
            }

            _paramsFlow.value = Params(wallet, currency, isOnline, pushes, getApps(wallet, pushes))
            if (!isOnline) {
                return@combine null
            }

            if (tokenRepositoryV2.doRequest(wallet.accountId, wallet.testnet, currency)) {
                setStatus(Item.Status.Default)
            } else { }

            subscribe(wallet.accountId, wallet.testnet, currency)
        }.launchIn(viewModelScope)

        combine(
            tokensFlow,
            statusFlow,
        ) { tokens, status ->
            val (fiatBalance, uiItems) = buildUiItems(tokens.currency, tokens.wallet.testnet, tokens.list)
            val balanceFormat = if (tokens.wallet.testnet) {
                CurrencyFormatter.formatFiat("TON", fiatBalance)
            } else {
                CurrencyFormatter.formatFiat(tokens.currency.code, fiatBalance)
            }

            val actualStatus = if (tokens.isOnline) {
                status
            } else {
                Item.Status.NoInternet
            }
            setItems(tokens.wallet, balanceFormat, uiItems, actualStatus, tokens.push, tokens.apps)
        }.launchIn(viewModelScope)
    }

    fun nextWallet() {
        viewModelScope.launch {
            val wallets = walletRepository.walletsFlow.firstOrNull() ?: return@launch
            val activeWallet = walletRepository.activeWalletFlow.firstOrNull() ?: return@launch
            val index = wallets.indexOf(activeWallet)
            val nextIndex = if (index == wallets.size - 1) 0 else index + 1
            walletRepository.setActiveWallet(wallets[nextIndex].id)
        }
    }

    fun prevWallet() {
        viewModelScope.launch {
            val wallets = walletRepository.walletsFlow.firstOrNull() ?: return@launch
            val activeWallet = walletRepository.activeWalletFlow.firstOrNull() ?: return@launch
            val index = wallets.indexOf(activeWallet)
            val prevIndex = if (index == 0) wallets.size - 1 else index - 1
            walletRepository.setActiveWallet(wallets[prevIndex].id)
        }
    }

    private fun setStatus(status: Item.Status) {
        _statusFlow.tryEmit(status)
    }

    private fun getApps(wallet: WalletEntity, events: List<AppPushEntity>): List<DAppEntity> {
        val dappUrls = events.map { it.dappUrl }
        return tonConnectRepository.getApps(dappUrls, wallet)
    }

    private fun buildUiItems(
        currency: WalletCurrency,
        testnet: Boolean,
        tokens: List<AccountTokenEntity>,
    ): Pair<Float, List<Item.Token>> {
        var fiatBalance = 0f
        if (testnet) {
            fiatBalance = tokens.first().balance.value
        }
        val uiItems = mutableListOf<Item.Token>()
        for ((index, token) in tokens.withIndex()) {
            fiatBalance += token.fiat

            val balanceFormat = CurrencyFormatter.format(value = token.balance.stakeOrTokenValue)
            val fiatFormat = CurrencyFormatter.formatFiat(currency.code, token.fiat)

            val item = Item.Token(
                position = ListCell.getPosition(tokens.size, index),
                iconUri = token.imageUri,
                address = token.address,
                symbol = token.symbol,
                name = token.name,
                balance = token.balance.stakeOrTokenValue,
                balanceFormat = balanceFormat,
                fiat = token.fiat,
                fiatFormat = fiatFormat,
                rate = CurrencyFormatter.formatFiat(currency.code, token.rateNow),
                rateDiff24h = token.rateDiff24h,
                verified = token.verified,
                testnet = testnet,
                stake = token.balance.stake,
                hiddenBalance = settings.hiddenBalances
            )
            uiItems.add(item)
        }
        return Pair(fiatBalance, uiItems)
    }

    private fun setItems(
        wallet: WalletEntity,
        balance: CharSequence,
        list: List<Item.Token>,
        status: Item.Status,
        push: List<AppPushEntity>,
        apps: List<DAppEntity>,
    ) {
        val items = mutableListOf<Item>()
        items.add(Item.Balance(
            balance = balance,
            address = wallet.address,
            walletType = wallet.type,
            status = status,
            hiddenBalance = settings.hiddenBalances
        ))
        items.add(Item.Actions(
            address = wallet.address,
            token = TokenEntity.TON,
            walletType = wallet.type,
            swapUri = api.config.swapUri,
            disableSwap = api.config.flags.disableSwap
        ))
        if (push.isNotEmpty()) {
            items.add(Item.Push(push, apps))
        }
        items.add(Item.Space(true))
        items.addAll(list)
        items.add(Item.Space(true))
        _uiItemsFlow.value = items.toList()
        setCached(wallet, items)
    }

    private fun setCached(wallet: WalletEntity, items: List<Item>) {
        screenCacheSource.set(CACHE_NAME, wallet.accountId, wallet.testnet, items)
    }








    private var subscriptionScope: CoroutineScope? = null

    private fun subscribe(accountId: String, testnet: Boolean, currency: WalletCurrency) {
        unsubscribe()

        subscriptionScope = CoroutineScope(Dispatchers.IO)
        subscriptionScope!!.launch {
            while (true) {
                delay(30000)

                tokenRepositoryV2.doRequestWithoutAllPools(accountId, testnet, currency)
            }
        }
    }

    private fun unsubscribe() {
        subscriptionScope?.cancel()
        subscriptionScope = null
    }

    override fun onCleared() {
        unsubscribe()
        super.onCleared()
    }


    companion object {
        private const val CACHE_NAME = "wallet"

        fun ScreenCacheSource.getWalletScreen(wallet: WalletEntity): List<Item>? {
            val items: List<Item> = get(CACHE_NAME, wallet.accountId, wallet.testnet) { parcel ->
                Item.createFromParcel(parcel)
            }.map {
                if (it is Item.Balance) {
                    it.copy(status = Item.Status.Updating)
                } else {
                    it
                }
            }
            if (items.isEmpty()) {
                return null
            }
            return items
        }
    }
}