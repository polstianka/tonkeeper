package com.tonapps.tonkeeper.ui.screen.root

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.setCustomKeys
import com.google.firebase.ktx.Firebase
import com.tonapps.extensions.MutableEffectFlow
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.ledger.ton.LedgerConnectData
import com.tonapps.tonkeeper.core.AnalyticsHelper
import com.tonapps.tonkeeper.core.deeplink.DeepLink
import com.tonapps.tonkeeper.core.entities.WalletExtendedEntity
import com.tonapps.tonkeeper.core.history.HistoryHelper
import com.tonapps.tonkeeper.core.history.list.item.HistoryItem
import com.tonapps.tonkeeper.core.signer.SingerArgs
import com.tonapps.tonkeeper.core.widget.Widget
import com.tonapps.tonkeeper.helper.ShortcutHelper
import com.tonapps.wallet.data.push.GooglePushService
import com.tonapps.tonkeeper.sign.SignManager
import com.tonapps.tonkeeper.ui.base.BaseWalletVM
import com.tonapps.tonkeeper.ui.screen.init.list.AccountItem
import com.tonapps.tonkeeper.ui.screen.main.MainScreen
import com.tonapps.tonkeeper.ui.screen.wallet.picker.list.WalletPickerAdapter
import com.tonapps.tonkeeper.ui.screen.wallet.main.WalletViewModel.Companion.getWalletScreen
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.Item
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.WalletAdapter
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.account.AccountRepository
import com.tonapps.wallet.data.core.ScreenCacheSource
import com.tonapps.wallet.data.core.Theme
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.core.entity.SignRequestEntity
import com.tonapps.wallet.data.passcode.PasscodeManager
import com.tonapps.wallet.data.purchase.PurchaseRepository
import com.tonapps.wallet.data.settings.BatteryTransaction
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.TokenRepository
import com.tonapps.wallet.data.tonconnect.TonConnectRepository
import com.tonapps.wallet.data.tonconnect.entities.DConnectEntity
import com.tonapps.wallet.data.tonconnect.entities.DAppEventEntity
import com.tonapps.wallet.data.tonconnect.entities.DAppRequestEntity
import com.tonapps.wallet.data.tonconnect.entities.reply.DAppSuccessEntity
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import uikit.extensions.collectFlow
import uikit.extensions.context
import uikit.navigation.Navigation.Companion.navigation

class RootViewModel(
    app: Application,
    private val passcodeManager: PasscodeManager,
    private val settingsRepository: SettingsRepository,
    private val accountRepository: AccountRepository,
    private val signManager: SignManager,
    private val tonConnectRepository: TonConnectRepository,
    private val api: API,
    private val historyHelper: HistoryHelper,
    private val screenCacheSource: ScreenCacheSource,
    private val walletAdapter: WalletAdapter,
    private val walletPickerAdapter: WalletPickerAdapter,
    private val tokenRepository: TokenRepository,
    private val purchaseRepository: PurchaseRepository
): BaseWalletVM(app) {

    data class Passcode(
        val show: Boolean,
        val biometric: Boolean,
    )

    private val selectedWalletFlow: Flow<WalletEntity> = accountRepository.selectedWalletFlow

    private val _hasWalletFlow = MutableEffectFlow<Boolean?>()
    val hasWalletFlow = _hasWalletFlow.asSharedFlow().filterNotNull()

    private val _eventFlow = MutableSharedFlow<RootEvent>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val eventFlow = _eventFlow.asSharedFlow().filterNotNull()

    private val _passcodeFlow = MutableStateFlow<Passcode?>(null)
    val passcodeFlow = _passcodeFlow.asStateFlow().filterNotNull()

    val theme: Theme
        get() = settingsRepository.theme

    val themeFlow: Flow<Int> = settingsRepository.themeFlow.map { it.resId }.drop(1)

    val tonConnectEventsFlow = tonConnectRepository.eventsFlow

    init {
        combine(
            settingsRepository.biometricFlow.take(1),
            settingsRepository.lockscreenFlow.take(1)
        ) { biometric, lockscreen ->
            Passcode(lockscreen, biometric)
        }.onEach {
            _passcodeFlow.value = it
        }.launchIn(viewModelScope)

        combine(
            accountRepository.selectedStateFlow.filter { it !is AccountRepository.SelectedState.Initialization },
            api.configFlow,
        ) { state, _ ->
            if (state is AccountRepository.SelectedState.Empty) {
                _hasWalletFlow.tryEmit(false)
                ShortcutManagerCompat.removeAllDynamicShortcuts(context)
            } else if (state is AccountRepository.SelectedState.Wallet) {
                val items = screenCacheSource.getWalletScreen(state.wallet) ?: listOf(Item.Skeleton(true))
                submitWalletList(items)
            }
            Widget.updateAll()
        }.flowOn(Dispatchers.IO).launchIn(viewModelScope)

        combine(
            selectedWalletFlow,
            settingsRepository.hiddenBalancesFlow
        ) { wallet, hiddenBalance ->
            val wallets = accountRepository.getWallets()
                .map { WalletExtendedEntity( it, settingsRepository.getWalletPrefs(it.id)) }
                .sortedBy { it.index }
                .map { it.raw }
            val balances = getBalances(wallets)
            walletPickerAdapter.submitList(WalletPickerAdapter.map(wallets, wallet, balances, hiddenBalance))
        }.launchIn(viewModelScope)

        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.firebaseToken = GooglePushService.requestToken()
        }

        collectFlow(selectedWalletFlow) { wallet ->
            applyAnalyticsKeys(wallet)
            initShortcuts(wallet)
        }

        collectFlow(api.configFlow.take(1)) { config ->
            AnalyticsHelper.setConfig(context, config)
            AnalyticsHelper.trackEvent("launch_app")
        }

        settingsRepository.countryFlow.take(1).filter { it.isBlank() }.map {
            api.resolveCountry()
        }.filterNotNull().onEach {
            settingsRepository.country = it
        }.flowOn(Dispatchers.IO).launchIn(viewModelScope)
    }

    private suspend fun initShortcuts(
        currentWallet: WalletEntity
    ) = withContext(Dispatchers.IO) {
        val wallets = accountRepository.getWallets()
        val list = mutableListOf<ShortcutInfoCompat>()
        if (!currentWallet.testnet) {
            list.add(ShortcutHelper.shortcutAction(context, Localization.send, R.drawable.ic_send_shortcut, "ton://"))
        }
        list.addAll(walletShortcutsFromWallet(currentWallet, wallets))
        ShortcutManagerCompat.setDynamicShortcuts(context, list.take(3))
    }

    private suspend fun walletShortcutsFromWallet(
        currentWallet: WalletEntity,
        wallets: List<WalletEntity>
    ): List<ShortcutInfoCompat> {
        val list = mutableListOf<ShortcutInfoCompat>()
        if (1 >= wallets.size) {
            return list
        }
        for (wallet in wallets) {
            if (wallet == currentWallet) {
                continue
            }
            list.add(ShortcutHelper.shortcutWallet(context, wallet))
        }
        return list
    }

    private fun applyAnalyticsKeys(wallet: WalletEntity) {
        val crashlytics = Firebase.crashlytics
        // crashlytics.setUserId(wallet.accountId)
        crashlytics.setCustomKeys {
            key("testnet", wallet.testnet)
            key("walletType", wallet.type.name)
        }
    }

    private suspend fun submitWalletList(items: List<Item>) = withContext(Dispatchers.Main) {
        walletAdapter.submitList(items) {
            _hasWalletFlow.tryEmit(true)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            accountRepository.logout()
            passcodeManager.reset()
            hidePasscode()
        }
    }

    private fun hidePasscode() {
        _passcodeFlow.value = Passcode(show = false, biometric = false)
    }

    suspend fun tonconnectReject(requestId: String, app: DConnectEntity) {
        tonConnectRepository.sendError(requestId, app, 300, "Reject Request")
    }

    suspend fun tonconnectBoc(
        requestId: String,
        app: DConnectEntity,
        boc: String
    ) {
        tonConnectRepository.send(requestId, app, boc)
    }

    suspend fun requestSign(
        context: Context,
        request: SignRequestEntity,
        batteryTxType: BatteryTransaction? = null,
        forceRelayer: Boolean = false,
    ): String {
        val wallet = selectedWalletFlow.firstOrNull() ?: throw Exception("wallet is null")
        return requestSign(context, wallet, request, batteryTxType, forceRelayer)
    }

    suspend fun requestSign(
        context: Context,
        wallet: WalletEntity,
        request: SignRequestEntity,
        batteryTxType: BatteryTransaction? = null,
        forceRelayer: Boolean = false,
    ): String {
        val navigation = context.navigation ?: throw Exception("navigation is null")
        return signManager.action(navigation, wallet, request, batteryTxType = batteryTxType, forceRelayer = forceRelayer)
    }

    suspend fun tonconnectBridgeEvent(
        context: Context,
        url: String,
        array: JSONArray
    ): String? {
        if (array.length() != 1) {
            throw IllegalStateException("Invalid array length")
        }
        return tonconnectBridgeEvent(context, url, array.getJSONObject(0))
    }

    suspend fun tonconnectBridgeEvent(
        context: Context,
        url: String,
        json: JSONObject
    ): String? {
        val wallet = selectedWalletFlow.firstOrNull() ?: throw IllegalStateException("No active wallet")
        val app = tonConnectRepository.getConnect(url, wallet) ?: throw IllegalStateException("No app")
        val event = DAppEventEntity(wallet.copy(), app.copy(), json)
        if (event.method != "sendTransaction") {
            throw IllegalStateException("Invalid method")
        }
        val params = event.params
        if (params.length() != 1) {
            throw IllegalStateException("Invalid params length")
        }
        val param = DAppEventEntity.parseParam(params.get(0))
        val request = SignRequestEntity(param)

        val boc = requestSign(context, event.wallet, request)
        val data = DAppSuccessEntity(event.id, boc)
        return data.toJSON().toString()
    }

    fun connectLedger(connectData: LedgerConnectData, accounts: List<AccountItem>) {
        _eventFlow.tryEmit(RootEvent.Ledger(connectData, accounts))
    }

    fun checkPasscode(context: Context, code: String): Flow<Unit> = flow {
        val valid = passcodeManager.isValid(context, code)
        if (valid) {
            hidePasscode()
            emit(Unit)
        } else {
            throw Exception("invalid passcode")
        }
    }.take(1)

    fun processDeepLink(uri: Uri, fromQR: Boolean, refSource: Uri?): Boolean {
        if (DeepLink.isSupportedUri(uri)) {
            resolveDeepLink(uri, fromQR, refSource)
            return true
        }
        return false
    }

    private fun resolveDeepLink(uri: Uri, fromQR: Boolean, refSource: Uri?) {
        if (uri.host == "signer") {
            collectFlow(hasWalletFlow.take(1)) {
                delay(1000)
                resolveSignerLink(uri, fromQR)
            }
            return
        }
        selectedWalletFlow.take(1).onEach { wallet ->
            delay(1000)
            resolveOther(refSource, uri, wallet)
        }.launchIn(viewModelScope)
    }

    private fun resolveSignerLink(uri: Uri, fromQR: Boolean) {
        try {
            val args = SingerArgs(uri)
            _eventFlow.tryEmit(RootEvent.Singer(args.publicKeyEd25519, args.name, fromQR))
        } catch (e: Throwable) {
            toast(Localization.invalid_link)
        }
    }

    private fun resolveOther(
        refSource: Uri?,
        _uri: Uri,
        wallet: WalletEntity
    ) {
        val url = _uri.toString().replace("ton://", "https://app.tonkeeper.com/").replace("tonkeeper://", "https://app.tonkeeper.com/")
        val uri = Uri.parse(url)
        val path = uri.path

        if (DeepLink.isTonConnectUri(uri)) {
            resolveTonConnect(uri, wallet, refSource)
        } else if (MainScreen.isSupportedDeepLink(url) || MainScreen.isSupportedDeepLink(_uri.toString())) {
            _eventFlow.tryEmit(RootEvent.OpenTab(_uri.toString()))
        } else if (path?.startsWith("/staking") == true) {
            _eventFlow.tryEmit(RootEvent.Staking)
        } else if (path?.startsWith("/pool/") == true) {
            _eventFlow.tryEmit(RootEvent.StakingPool(uri.pathSegments.last()))
        } else if (path?.startsWith("/action/") == true) {
            val actionId = uri.pathSegments.last()
            val accountAddress = uri.getQueryParameter("account")
            if (accountAddress == null) {
                showTransaction(actionId)
            } else {
                showTransaction(accountAddress, actionId)
            }
        } else if (path?.startsWith("/transfer/") == true) {
            _eventFlow.tryEmit(RootEvent.Transfer(
                address = uri.pathSegments.last(),
                amount = uri.getQueryParameter("amount"),
                text = uri.getQueryParameter("text"),
                jettonAddress = uri.getQueryParameter("jetton"),
            ))
        } else if (path?.startsWith("/action/") == true) {
            val account = uri.getQueryParameter("account") ?: return
            val hash = uri.pathSegments.lastOrNull() ?: return
            showTransaction(account, hash)
        } else if (path?.startsWith("/pick/") == true) {
            val walletId = uri.pathSegments.lastOrNull() ?: return
            viewModelScope.launch { accountRepository.setSelectedWallet(walletId) }
        } else if (path?.startsWith("/swap") == true) {
            val ft = uri.getQueryParameter("ft") ?: "TON"
            val tt = uri.getQueryParameter("tt")
            _eventFlow.tryEmit(RootEvent.Swap(api.config.swapUri, wallet.address, ft, tt))
        } else if (path?.startsWith("/battery") == true) {
            val promocode = uri.getQueryParameter("promocode")
            _eventFlow.tryEmit(RootEvent.Battery(promocode))
        } else if (path?.startsWith("/buy-ton") == true || uri.path == "/exchange" || uri.path == "/exchange/") {
            _eventFlow.tryEmit(RootEvent.BuyOrSell())
        } else if (path?.startsWith("/exchange") == true) {
            val name = uri.pathSegments.lastOrNull() ?: return
            val method = purchaseRepository.getMethod(name, wallet.testnet, settingsRepository.getLocale())
            _eventFlow.tryEmit(RootEvent.BuyOrSell(method))
        } else if (path?.startsWith("/backups") == true) {
            _eventFlow.tryEmit(RootEvent.OpenBackups)
        } else {
            toast(Localization.invalid_link)
        }
    }

    private fun showTransaction(hash: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val wallet = selectedWalletFlow.firstOrNull() ?: return@launch
            val item = historyHelper.getEvent(wallet, hash).filterIsInstance<HistoryItem.Event>().firstOrNull() ?: return@launch
            _eventFlow.tryEmit(RootEvent.Transaction(item))
        }
    }

    private fun showTransaction(accountId: String, hash: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val wallet = accountRepository.getWalletByAccountId(accountId, false) ?: return@launch
            val event = api.getTransactionEvents(wallet.accountId, wallet.testnet, hash) ?: return@launch
            val item = historyHelper.mapping(wallet, event).find { it is HistoryItem.Event } as? HistoryItem.Event ?: return@launch
            _eventFlow.tryEmit(RootEvent.Transaction(item))
        }
    }

    private fun resolveTonConnect(
        uri: Uri,
        wallet: WalletEntity,
        source: Uri?
    ) {
        try {
            if (!wallet.hasPrivateKey && !wallet.isLedger) {
                toast(Localization.not_supported)
                return
            }
            val request = DAppRequestEntity(source, uri)
            _eventFlow.tryEmit(RootEvent.TonConnect(request))
        } catch (e: Throwable) {
            toast(Localization.invalid_link)
        }
    }

    private fun toast(resId: Int) {
        _eventFlow.tryEmit(RootEvent.Toast(resId))
    }

    private suspend fun getBalances(
        wallets: List<WalletEntity>
    ): List<CharSequence> = withContext(Dispatchers.IO) {
        val list = mutableListOf<Deferred<CharSequence>>()
        for (wallet in wallets) {
            list.add(async { getBalance(wallet.accountId, wallet.testnet) })
        }
        list.map { it.await() }
    }

    private suspend fun getBalance(
        accountId: String,
        testnet: Boolean
    ): CharSequence {
        val currency = if (testnet) {
            WalletCurrency.TON
        } else {
            settingsRepository.currency
        }
        val totalBalance = tokenRepository.getTotalBalances(currency, accountId, testnet) ?: return context.getString(Localization.unknown)
        return CurrencyFormatter.formatFiat(currency.code, totalBalance)
    }
}