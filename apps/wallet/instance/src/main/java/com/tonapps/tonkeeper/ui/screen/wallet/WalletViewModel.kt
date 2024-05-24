package com.tonapps.tonkeeper.ui.screen.wallet

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.network.NetworkMonitor
import com.tonapps.tonkeeper.fragment.stake.domain.StakingRepository
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakedBalance
import com.tonapps.tonkeeper.fragment.stake.domain.model.getTotalFiatBalance
import com.tonapps.tonkeeper.fragment.stake.domain.model.hasAddress
import com.tonapps.tonkeeper.fragment.swap.domain.DexAssetsRepository
import com.tonapps.tonkeeper.ui.screen.wallet.list.Item
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.account.entities.WalletEvent
import com.tonapps.wallet.data.account.legacy.WalletManager
import com.tonapps.wallet.data.core.ScreenCacheSource
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.push.PushManager
import com.tonapps.wallet.data.push.entities.AppPushEntity
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.TokenRepository
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import com.tonapps.wallet.data.tonconnect.TonConnectRepository
import com.tonapps.wallet.data.tonconnect.entities.DAppEntity
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uikit.extensions.collectFlow
import java.math.BigDecimal

class WalletViewModel(
    private val walletRepository: WalletRepository,
    private val settings: SettingsRepository,
    private val tokenRepository: TokenRepository,
    private val api: API,
    private val networkMonitor: NetworkMonitor,
    private val pushManager: PushManager,
    private val tonConnectRepository: TonConnectRepository,
    private val screenCacheSource: ScreenCacheSource,
    private val dexAssetsRepository: DexAssetsRepository,
    private val walletManager: WalletManager,
    private val stakingRepository: StakingRepository
) : ViewModel() {

    private data class Tokens(
        val wallet: WalletEntity,
        val currency: WalletCurrency,
        val list: List<AccountTokenEntity>,
        val isOnline: Boolean,
        val push: List<AppPushEntity>,
        val apps: List<DAppEntity>,
    )

    init {
        viewModelScope.launch(context = Dispatchers.IO) {
            val wallet = walletManager.getWalletInfo()
            dexAssetsRepository.loadAssets(wallet!!.address)
        }
    }

    private val _tokensFlow = MutableStateFlow<Tokens?>(null)
    private val tokensFlow =
        _tokensFlow.asStateFlow().filterNotNull().filter { it.list.isNotEmpty() }

    private val _lastLtFlow = MutableStateFlow<Long>(0)
    private val lastLtFlow = _lastLtFlow.asStateFlow()
    private val walletCurrencyPair = combine(
        walletRepository.activeWalletFlow,
        settings.currencyFlow
    ) { a, b -> a to b }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val stakedBalancesFlow = walletCurrencyPair.flatMapLatest { (wallet, currency) ->
        stakingRepository.getStakedBalanceFlow(wallet.address, currency, wallet.testnet)
    }

    private val _statusFlow =
        MutableSharedFlow<Item.Status>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val statusFlow = _statusFlow.asSharedFlow()

    private val _uiItemsFlow = MutableStateFlow<List<Item>>(emptyList())
    val uiItemsFlow = _uiItemsFlow.asStateFlow().filter { it.isNotEmpty() }

    val uiLabelFlow = walletRepository.activeWalletFlow.map { it.label }
    private val errorHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        Log.wtf("###", "error: ${throwable.message}")
    }

    init {
        walletRepository.activeWalletFlow.map { screenCacheSource.getWalletScreen(it) }
            .flowOn(Dispatchers.IO)
            .onEach { items ->
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

        collectFlow(walletCurrencyPair) { (activeWallet, currency) ->
            stakingRepository.loadStakedBalances(
                activeWallet.address,
                activeWallet.testnet
            )
        }

        combine(
            walletRepository.activeWalletFlow,
            settings.currencyFlow,
            networkMonitor.isOnlineFlow,
            lastLtFlow,
            pushManager.dAppPushFlow,
        ) { wallet, currency, isOnline, lastLt, push ->
            val pushes = push?.distinctBy { it.dappUrl } ?: emptyList()

            if (lastLt == 0L) {
                setStatus(Item.Status.Updating)
                _tokensFlow.value = getLocalTokens(wallet, currency, isOnline, pushes)
            }

            if (!isOnline) {
                return@combine null
            }

            getRemoteTokens(wallet, currency, pushes)?.let { tokens ->
                setStatus(Item.Status.Default)
                _tokensFlow.value = tokens
            }
        }.launchIn(viewModelScope)

        combine(
            tokensFlow,
            statusFlow,
            stakedBalancesFlow
        ) { tokens, status, stakedBalances ->
            val (fiatBalance, uiItems) = buildUiItems(
                tokens.currency,
                tokens.wallet.testnet,
                tokens.list,
                stakedBalances
            )
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

    private suspend fun getLocalTokens(
        wallet: WalletEntity,
        currency: WalletCurrency,
        isOnline: Boolean,
        push: List<AppPushEntity>
    ): Tokens? = withContext(Dispatchers.IO) {
        val tokens = tokenRepository.getLocal(currency, wallet.accountId, wallet.testnet)
        if (tokens.isEmpty()) {
            return@withContext null
        }

        Tokens(wallet, currency, tokens, isOnline, push, getApps(wallet, push))
    }

    private suspend fun getRemoteTokens(
        wallet: WalletEntity,
        currency: WalletCurrency,
        push: List<AppPushEntity>
    ): Tokens? = withContext(Dispatchers.IO) {
        try {
            val tokens = tokenRepository.getRemote(currency, wallet.accountId, wallet.testnet)
            Tokens(wallet, currency, tokens, true, push, getApps(wallet, push))
        } catch (e: Throwable) {
            null
        }
    }

    private fun getApps(wallet: WalletEntity, events: List<AppPushEntity>): List<DAppEntity> {
        val dappUrls = events.map { it.dappUrl }
        return tonConnectRepository.getApps(dappUrls, wallet)
    }

    private fun buildUiItems(
        currency: WalletCurrency,
        testnet: Boolean,
        tokens: List<AccountTokenEntity>,
        stakedBalances: List<StakedBalance>
    ): Pair<BigDecimal, List<Item>> {
        var fiatBalance = BigDecimal.ZERO
        if (testnet) {
            fiatBalance = tokens.first().balance.value
        }
        val tokenItemsPre = mutableListOf<Item.Token>()
        for ((index, token) in tokens.withIndex()) {
            if (!token.isTon && stakedBalances.any { it.hasAddress(token.address) }) {
                continue
            }
            fiatBalance += token.fiat

            val balanceFormat = CurrencyFormatter.format(value = token.balance.value)
            val fiatFormat = CurrencyFormatter.formatFiat(currency.code, token.fiat)
            val item = Item.Token(
                position = ListCell.getPosition(tokens.size, index),
                iconUri = token.imageUri,
                address = token.address,
                symbol = token.symbol,
                name = token.name,
                balance = token.balance.value,
                balanceFormat = balanceFormat,
                fiat = token.fiat,
                fiatFormat = fiatFormat,
                rate = CurrencyFormatter.formatFiat(currency.code, token.rateNow),
                rateDiff24h = token.rateDiff24h,
                verified = token.verified,
                testnet = testnet,
                hiddenBalance = settings.hiddenBalances
            )
            tokenItemsPre.add(item)
        }
        val tokensSize = tokenItemsPre.size
        val size = tokensSize + stakedBalances.size
        val stakedBalanceItems = stakedBalances.mapIndexed { index, stakedBalance ->
            Item.StakedItem(
                position = ListCell.getPosition(size, tokensSize + index),
                balance = stakedBalance,
            )
        }
        if (!testnet) {
            stakedBalanceItems.forEach {
                fiatBalance += it.balance.getTotalFiatBalance()
            }
        }
        val tokenItems = tokenItemsPre.mapIndexed { index, it ->
            it.copy(position = ListCell.getPosition(size, index))
        }
        return Pair(fiatBalance, tokenItems + stakedBalanceItems)
    }

    private fun setItems(
        wallet: WalletEntity,
        balance: CharSequence,
        list: List<Item>,
        status: Item.Status,
        push: List<AppPushEntity>,
        apps: List<DAppEntity>,
    ) {
        val items = mutableListOf<Item>()
        items.add(
            Item.Balance(
                balance = balance,
                address = wallet.address,
                walletType = wallet.type,
                status = status,
                hiddenBalance = settings.hiddenBalances
            )
        )
        items.add(
            Item.Actions(
                address = wallet.address,
                token = TokenEntity.TON,
                walletType = wallet.type,
                swapUri = api.config.swapUri,
                disableSwap = api.config.flags.disableSwap
            )
        )
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