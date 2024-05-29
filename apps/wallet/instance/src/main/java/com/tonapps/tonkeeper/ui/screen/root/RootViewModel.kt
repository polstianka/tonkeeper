package com.tonapps.tonkeeper.ui.screen.root

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.Person
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.setCustomKeys
import com.google.firebase.ktx.Firebase
import com.tonapps.blockchain.Coin
import com.tonapps.emoji.Emoji
import com.tonapps.extensions.MutableEffectFlow
import com.tonapps.extensions.getQueryLong
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.core.deeplink.DeepLink
import com.tonapps.tonkeeper.core.history.HistoryHelper
import com.tonapps.tonkeeper.core.history.list.item.HistoryItem
import com.tonapps.tonkeeper.core.signer.SingerArgs
import com.tonapps.tonkeeper.core.widget.Widget
import com.tonapps.tonkeeper.helper.ShortcutHelper
import com.tonapps.tonkeeper.password.PasscodeRepository
import com.tonapps.wallet.data.push.GooglePushService
import com.tonapps.wallet.data.push.PushManager
import com.tonapps.tonkeeper.sign.SignManager
import com.tonapps.tonkeeper.sign.SignRequestEntity
import com.tonapps.tonkeeper.ui.screen.main.MainScreen
import com.tonapps.tonkeeper.ui.screen.picker.list.WalletPickerAdapter
import com.tonapps.tonkeeper.ui.screen.wallet.WalletViewModel.Companion.getWalletScreen
import com.tonapps.tonkeeper.ui.screen.wallet.list.Item
import com.tonapps.tonkeeper.ui.screen.wallet.list.WalletAdapter
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.accentBlueColor
import com.tonapps.uikit.icon.UIKitIcon
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.account.WalletSource
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.core.ScreenCacheSource
import com.tonapps.wallet.data.core.Theme
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.TokenRepository
import com.tonapps.wallet.data.tonconnect.TonConnectRepository
import com.tonapps.wallet.data.tonconnect.entities.DAppEntity
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import uikit.extensions.collectFlow
import uikit.navigation.Navigation.Companion.navigation

class RootViewModel(
    application: Application,
    private val passcodeRepository: PasscodeRepository,
    private val settingsRepository: SettingsRepository,
    private val walletRepository: WalletRepository,
    private val signManager: SignManager,
    private val tonConnectRepository: TonConnectRepository,
    private val api: API,
    private val historyHelper: HistoryHelper,
    private val screenCacheSource: ScreenCacheSource,
    private val walletAdapter: WalletAdapter,
    private val walletPickerAdapter: WalletPickerAdapter,
    private val tokenRepository: TokenRepository
): AndroidViewModel(application) {

    data class Passcode(
        val show: Boolean,
        val biometric: Boolean,
    )

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
        _passcodeFlow.value = Passcode(
            show = settingsRepository.lockScreen && passcodeRepository.hasPinCode,
            biometric = settingsRepository.biometric
        )

        walletRepository.walletsFlow.onEach { wallets ->
            if (wallets.isEmpty()) {
                ShortcutManagerCompat.removeAllDynamicShortcuts(application)
                _hasWalletFlow.tryEmit(false)
            } else {
                val wallet = walletRepository.activeWalletFlow.first()
                val items = screenCacheSource.getWalletScreen(wallet)
                if (items.isNullOrEmpty()) {
                    _hasWalletFlow.tryEmit(true)
                } else {
                    submitWalletList(items)
                }
            }
            Widget.updateAll()
        }.flowOn(Dispatchers.IO).launchIn(viewModelScope)

        combine(
            walletRepository.walletsFlow,
            walletRepository.activeWalletFlow,
            settingsRepository.hiddenBalancesFlow,
        ) { wallets, wallet, hiddenBalance ->
            val balances = getBalances(wallets)
            walletPickerAdapter.submitList(WalletPickerAdapter.map(wallets, wallet, balances, hiddenBalance))
        }.launchIn(viewModelScope)

        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.firebaseToken = GooglePushService.requestToken()
        }

        collectFlow(walletRepository.activeWalletFlow, ::applyAnalyticsKeys)

        combine(walletRepository.activeWalletFlow, walletRepository.walletsFlow, ::initShortcuts).flowOn(Dispatchers.IO).launchIn(viewModelScope)
    }

    private suspend fun initShortcuts(
        currentWallet: WalletEntity,
        wallets: List<WalletEntity>
    ) {
        val context = getApplication<App>()
        val list = mutableListOf<ShortcutInfoCompat>()
        if (!currentWallet.testnet) {
            list.add(ShortcutHelper.shortcutAction(context, Localization.send, R.drawable.ic_send_shortcut, "ton://"))
        }
        list.addAll(walletShortcutsFromWallet(currentWallet, wallets))
        ShortcutManagerCompat.setDynamicShortcuts(context, list)
    }

    private suspend fun walletShortcutsFromWallet(
        currentWallet: WalletEntity,
        wallets: List<WalletEntity>
    ): List<ShortcutInfoCompat> {
        val context = getApplication<App>()
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
        crashlytics.setUserId(wallet.accountId)
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
            walletRepository.clear()
            passcodeRepository.clear()
            hidePasscode()
        }
    }

    private fun hidePasscode() {
        _passcodeFlow.value = Passcode(show = false, biometric = false)
    }

    suspend fun tonconnectReject(requestId: String, app: DAppEntity) {
        tonConnectRepository.sendError(requestId, app, 300, "Reject Request")
    }

    suspend fun tonconnectBoc(
        requestId: String,
        app: DAppEntity,
        boc: String
    ) {
        tonConnectRepository.send(requestId, app, boc)
    }

    suspend fun requestSign(
        context: Context,
        request: SignRequestEntity
    ): String {
        val wallet = walletRepository.activeWalletFlow.firstOrNull() ?: throw Exception("wallet is null")
        return requestSign(context, wallet, request)
    }

    suspend fun requestSign(
        context: Context,
        wallet: WalletEntity,
        request: SignRequestEntity
    ): String {
        val navigation = context.navigation ?: throw Exception("navigation is null")
        return signManager.action(navigation, wallet, request)
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
        val wallet = walletRepository.activeWalletFlow.firstOrNull() ?: throw IllegalStateException("No active wallet")
        val app = tonConnectRepository.getApp(url, wallet) ?: throw IllegalStateException("No app")
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

    fun checkPasscode(code: String): Flow<Unit> = flow {
        val valid = passcodeRepository.compare(code)
        if (valid) {
            hidePasscode()
            emit(Unit)
        } else {
            throw Exception("invalid passcode")
        }
    }.take(1)

    fun processDeepLink(uri: Uri, fromQR: Boolean): Boolean {
        if (DeepLink.isSupportedUri(uri)) {
            resolveDeepLink(uri, fromQR)
            return true
        }
        return false
    }

    private fun resolveDeepLink(uri: Uri, fromQR: Boolean) {
        if (uri.host == "signer") {
            collectFlow(hasWalletFlow.take(1)) {
                delay(1000)
                resolveSignerLink(uri, fromQR)
            }
            return
        }
        walletRepository.activeWalletFlow.take(1).onEach { wallet ->
            delay(1000)
            resolveOther(uri, wallet)
        }.launchIn(viewModelScope)
    }

    private fun resolveSignerLink(uri: Uri, fromQR: Boolean) {
        try {
            val args = SingerArgs(uri)
            val walletSource = if (fromQR) {
                WalletSource.SingerQR
            } else {
                WalletSource.SingerApp
            }
            _eventFlow.tryEmit(RootEvent.Singer(args.publicKeyEd25519, args.name, walletSource))
        } catch (e: Throwable) {
            toast(Localization.invalid_link)
        }
    }

    fun goHistory() {
        _eventFlow.tryEmit(RootEvent.OpenTab("tonkeeper://activity"))
    }

    private fun resolveOther(_uri: Uri, wallet: WalletEntity) {
        val url = _uri.toString().replace("ton://", "https://app.tonkeeper.com/").replace("tonkeeper://", "https://app.tonkeeper.com/")
        val uri = Uri.parse(url)
        if (DeepLink.isTonConnectUri(uri)) {
            resolveTonConnect(uri, wallet)
        } else if (MainScreen.isSupportedDeepLink(url) || MainScreen.isSupportedDeepLink(_uri.toString())) {
            _eventFlow.tryEmit(RootEvent.OpenTab(_uri.toString()))
        } else if (uri.path?.startsWith("/transfer/") == true) {
            _eventFlow.tryEmit(RootEvent.Transfer(
                address = uri.pathSegments.last(),
                amount = uri.getQueryLong("amount")?.let { Coin.toCoins(it) },
                text = uri.getQueryParameter("text"),
                jettonAddress = uri.getQueryParameter("jetton"),
            ))
        } else if (uri.path?.startsWith("/action/") == true) {
            val account = uri.getQueryParameter("account") ?: return
            val hash = uri.pathSegments.lastOrNull() ?: return
            showTransaction(account, hash)
        } else if (uri.path?.startsWith("/pick/") == true) {
            val walletId = uri.pathSegments.lastOrNull()?.toLongOrNull() ?: return
            viewModelScope.launch { walletRepository.setActiveWallet(walletId) }
        } else if (uri.path?.startsWith("/swap") == true) {
            val ft = uri.getQueryParameter("ft") ?: "TON"
            val tt = uri.getQueryParameter("tt")
            _eventFlow.tryEmit(RootEvent.Swap(api.config.swapUri, wallet.address, ft, tt))
        } else if (uri.path?.startsWith("/buy-ton") == true || uri.path == "/exchange" || uri.path == "/exchange/") {
            _eventFlow.tryEmit(RootEvent.BuyOrSell)
        } else if (uri.path?.startsWith("/exchange") == true) {
            val name = uri.pathSegments.lastOrNull() ?: return
            _eventFlow.tryEmit(RootEvent.BuyOrSellDirect(name))
        } else {
            Log.d("DeepLinkLog", "uri: $uri")
            Log.d("DeepLinkLog", "path segments: ${uri.pathSegments}")
            Log.d("DeepLinkLog", "path: ${uri.path}")
            toast(Localization.invalid_link)
        }
    }

    private fun showTransaction(accountId: String, hash: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val wallet = walletRepository.getWallet(accountId) ?: return@launch
            val event = api.getTransactionEvents(wallet.accountId, wallet.testnet, hash) ?: return@launch
            val item = historyHelper.mapping(wallet, event).find { it is HistoryItem.Event } as? HistoryItem.Event ?: return@launch
            _eventFlow.tryEmit(RootEvent.Transaction(item))
        }
    }

    private fun resolveTonConnect(
        uri: Uri,
        wallet: WalletEntity
    ) {
        try {
            if (!wallet.hasPrivateKey) {
                toast(Localization.not_supported)
                return
            }
            val request = DAppRequestEntity(uri)
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
        val totalBalance = tokenRepository.getTotalBalances(currency, accountId, testnet)
        return CurrencyFormatter.formatFiat(currency.code, totalBalance.toFloat())
    }
}