package com.tonapps.tonkeeper.manager.tonconnect

import android.content.Context
import android.net.Uri
import android.util.ArrayMap
import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.tonapps.blockchain.ton.extensions.equalsAddress
import com.tonapps.blockchain.ton.proof.TONProof
import com.tonapps.extensions.appVersionName
import com.tonapps.extensions.bestMessage
import com.tonapps.extensions.filterList
import com.tonapps.extensions.flat
import com.tonapps.extensions.flatter
import com.tonapps.extensions.hasQuery
import com.tonapps.extensions.isEmptyQuery
import com.tonapps.extensions.mapList
import com.tonapps.extensions.toUriOrNull
import com.tonapps.network.simple
import com.tonapps.security.CryptoBox
import com.tonapps.tonkeeper.client.safemode.SafeModeClient
import com.tonapps.tonkeeper.core.DevSettings
import com.tonapps.tonkeeper.extensions.isSafeModeEnabled
import com.tonapps.tonkeeper.extensions.showToast
import com.tonapps.tonkeeper.manager.push.PushManager
import com.tonapps.tonkeeper.manager.tonconnect.bridge.Bridge
import com.tonapps.tonkeeper.manager.tonconnect.bridge.JsonBuilder
import com.tonapps.tonkeeper.manager.tonconnect.bridge.model.BridgeError
import com.tonapps.tonkeeper.manager.tonconnect.bridge.model.BridgeEvent
import com.tonapps.tonkeeper.manager.tonconnect.bridge.model.BridgeMethod
import com.tonapps.tonkeeper.manager.tonconnect.exceptions.ManifestException
import com.tonapps.tonkeeper.ui.screen.tonconnect.TonConnectSafeModeDialog
import com.tonapps.tonkeeper.ui.screen.tonconnect.TonConnectScreen
import com.tonapps.tonkeeper.worker.DAppPushToggleWorker
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.AccountRepository
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.dapps.DAppsRepository
import com.tonapps.wallet.data.dapps.entities.AppConnectEntity
import com.tonapps.wallet.data.dapps.entities.AppEntity
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import uikit.extensions.activity
import uikit.extensions.addForResult
import uikit.navigation.NavigationActivity
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicBoolean

class TonConnectManager(
    private val scope: CoroutineScope,
    private val api: API,
    private val accountRepository: AccountRepository,
    private val dAppsRepository: DAppsRepository,
    private val pushManager: PushManager,
    private val safeModeClient: SafeModeClient,
    private val settingsRepository: SettingsRepository,
) {

    private val bridge: Bridge = Bridge(api)
    private val bridgeConnected = AtomicBoolean(false)
    private var bridgeJob: Job? = null

    private val _eventsFlow = MutableSharedFlow<BridgeEvent>(replay = 1)
    private val eventsFlow = _eventsFlow.asSharedFlow().mapNotNull { event ->
        val lastAppRequestId = dAppsRepository.getLastAppRequestId(event.connection.clientId)
        if (lastAppRequestId >= event.message.id) {
            DevSettings.tonConnectLog("Last app event id: $lastAppRequestId\nIgnore event: $event", error = true)
            return@mapNotNull null
        }
        event
    }.shareIn(scope, SharingStarted.Eagerly, 1)

    val transactionRequestFlow = eventsFlow.mapNotNull { event ->
        if (event.method == BridgeMethod.SEND_TRANSACTION) {
            Pair(event.connection, event.message)
        } else {
            if (event.method == BridgeMethod.DISCONNECT) {
                deleteConnection(event.connection, event.message.id)
            } else {
                sendBridgeError(event.connection, BridgeError.methodNotSupported("Method \"${event.method}\" not supported"), event.message.id)
            }
            null
        }
    }.flowOn(Dispatchers.IO).shareIn(scope, SharingStarted.Eagerly, 0)

    fun connectBridge() {
        if (bridgeConnected.get()) {
            return
        }
        bridgeJob?.cancel()
        bridgeConnected.set(true)

        bridgeJob = scope.launch(Dispatchers.IO) {
            val connections = dAppsRepository.getConnections().chunked(50)
            if (connections.isEmpty()) {
                return@launch
            }
            val flow = connections.map {
                bridge.eventsFlow(it, dAppsRepository.lastEventId)
            }.flatter()
            flow.collect {
                if (bridgeConnected.get()) {
                    _eventsFlow.emit(it)
                }
            }
        }
    }

    fun disconnectBridge() {
        if (!bridgeConnected.get()) {
            return
        }
        bridgeJob?.cancel()
        bridgeConnected.set(false)
    }

    private fun reconnectBridge() {
        if (bridgeConnected.get()) {
            disconnectBridge()
            connectBridge()
        }
    }

    fun walletConnectionsFlow(wallet: WalletEntity) = accountConnectionsFlow(wallet.accountId, wallet.testnet)

    fun accountConnectionsFlow(accountId: String, testnet: Boolean = false) = dAppsRepository.connectionsFlow.filterList { connection ->
        connection.testnet == testnet && connection.accountId.equalsAddress(accountId)
    }

    fun setLastAppRequestId(clientId: String, messageId: Long) {
        dAppsRepository.setLastAppRequestId(clientId, messageId)
    }

    fun walletAppsFlow(wallet: WalletEntity) = walletConnectionsFlow(wallet).mapList { it.appUrl }.map { it.distinct() }.map { urls ->
        dAppsRepository.getApps(urls)
    }.flowOn(Dispatchers.IO)

    private suspend fun deleteConnection(connection: AppConnectEntity, messageId: Long) {
        val deleted = dAppsRepository.deleteConnect(connection)
        if (!deleted) {
            sendBridgeError(connection, BridgeError.unknownApp("App not found. Request user to reconnect wallet for access restoration."), messageId)
        } else {
            bridge.sendDisconnectResponseSuccess(connection, messageId)
        }

        accountRepository.getWalletByAccountId(connection.accountId, connection.testnet)?.let {
            pushManager.dAppUnsubscribe(it, listOf(connection))
        }
    }

    suspend fun getConnection(
        accountId: String,
        testnet: Boolean,
        appUrl: Uri,
        type: AppConnectEntity.Type
    ): AppConnectEntity? {
        val apps = dAppsRepository.getConnections(accountId, testnet)
        if (apps.isEmpty) {
            return null
        }
        val connections = apps.filter {
            it.key.url == appUrl || it.key.url.host == appUrl.host
        }.values.flatten()
        for (connection in connections) {
            if (connection.type == type) {
                return connection
            }
        }
        return null
    }

    fun disconnect(wallet: WalletEntity, appUrl: Uri, type: AppConnectEntity.Type? = null) {
        scope.launch(Dispatchers.IO) {
            val connections = dAppsRepository.deleteApp(wallet.accountId, wallet.testnet, appUrl, type)
            for (connection in connections) {
                bridge.sendDisconnect(connection)
            }
            pushManager.dAppUnsubscribe(wallet, connections)
        }
    }

    suspend fun clear(wallet: WalletEntity) = withContext(Dispatchers.IO) {
        val connections = dAppsRepository.deleteApps(wallet.accountId, wallet.testnet)
        for (connection in connections) {
            bridge.sendDisconnect(connection)
        }
        pushManager.dAppUnsubscribe(wallet, connections)
    }

    suspend fun sendBridgeError(connection: AppConnectEntity, error: BridgeError, id: Long) {
        bridge.sendError(connection, error, id)
        setLastAppRequestId(connection.clientId, id)
    }

    suspend fun sendTransactionResponseSuccess(connection: AppConnectEntity, boc: String, id: Long) {
        bridge.sendTransactionResponseSuccess(connection, boc, id)
        setLastAppRequestId(connection.clientId, id)
    }

    fun isPushEnabled(wallet: WalletEntity, appUrl: Uri): Boolean {
        return dAppsRepository.isPushEnabled(wallet.accountId, wallet.testnet, appUrl)
    }

    private suspend fun newConnect(
        wallet: WalletEntity,
        keyPair: CryptoBox.KeyPair,
        clientId: String,
        appUrl: Uri,
        proof: TONProof.Result?,
        pushEnabled: Boolean,
        type: AppConnectEntity.Type
    ): AppConnectEntity = withContext(Dispatchers.IO) {
        val timestamp = proof?.timestamp ?: (System.currentTimeMillis() / 1000L)
        val connection = AppConnectEntity(
            accountId = wallet.accountId,
            testnet = wallet.testnet,
            clientId = clientId,
            type = type,
            appUrl = appUrl,
            keyPair = keyPair,
            proofSignature = proof?.signature,
            timestamp = timestamp,
            proofPayload = proof?.payload,
            pushEnabled = pushEnabled
        )
        if (!dAppsRepository.newConnect(connection)) {
            throw Exception("Failed to save connection")
        }
        connection
    }

    fun processDeeplink(
        context: Context,
        uri: Uri,
        fromQR: Boolean,
        refSource: Uri?,
        fromPackageName: String?
    ): Uri? {
        val returnUri = TonConnect.parseReturn(uri.getQueryParameter("ret"), refSource)
        try {
            val activity = context.activity ?: throw IllegalArgumentException("Context must be an Activity")
            val normalizedUri = normalizeUri(uri)
            val tonConnect = TonConnect.parse(
                uri = normalizedUri,
                refSource = refSource,
                fromQR = fromQR,
                returnUri = returnUri,
                fromPackageName = fromPackageName
            )

            safeModeClient.isReadyFlow.take(1).onEach {
                if (!isScam(context, WalletEntity.EMPTY, uri, normalizedUri, tonConnect.manifestUrl.toUri())) {
                    connectRemoteApp(activity, tonConnect)
                }
            }.launchIn(scope)
            return null
        } catch (e: Exception) {
            if (uri.isEmptyQuery || uri.hasQuery("open") || uri.hasQuery("ret")) {
                return returnUri
            } else {
                context.showToast(Localization.invalid_link)
                return null
            }
        }
    }

    private suspend fun connectRemoteApp(activity: NavigationActivity, tonConnect: TonConnect) {
        val keyPair = CryptoBox.keyPair()
        val message = launchConnectFlow(activity, tonConnect, keyPair, null)
        bridge.send(tonConnect.clientId, keyPair, message.toString())
    }

    suspend fun launchConnectFlow(
        activity: NavigationActivity,
        tonConnect: TonConnect,
        keyPair: CryptoBox.KeyPair = CryptoBox.keyPair(),
        wallet: WalletEntity?
    ): JSONObject = withContext(Dispatchers.IO) {
        if (tonConnect.request.items.isEmpty()) {
            return@withContext JsonBuilder.connectEventError(BridgeError.badRequest("Empty value provided in required field \"items\""))
        }

        val clientId = tonConnect.clientId
        try {

            val app = readManifest(tonConnect.manifestUrl)
            if (isScam(activity, wallet ?: WalletEntity.EMPTY, app.iconUrl.toUri(), app.url)) {
                return@withContext JsonBuilder.connectEventError(BridgeError.badRequest("client error"))
            }

            val screen = TonConnectScreen.newInstance(
                app = app,
                proofPayload = tonConnect.proofPayload,
                returnUri = tonConnect.returnUri,
                wallet = wallet,
                fromPackageName = tonConnect.fromPackageName
            )
            val bundle = activity.addForResult(screen)
            val response = screen.contract.parseResult(bundle)

            if (wallet != null && !wallet.isTonConnectSupported) {
                return@withContext JsonBuilder.connectEventError(BridgeError.methodNotSupported("Wallet not supported TonConnect"))
            }

            val connect = newConnect(
                wallet = response.wallet,
                keyPair = keyPair,
                clientId = clientId,
                appUrl = app.url,
                proof = response.proof,
                pushEnabled = response.notifications,
                type = if (tonConnect.jsInject) AppConnectEntity.Type.Internal else AppConnectEntity.Type.External
            )

            activity.runOnUiThread {
                reconnectBridge()

                DAppPushToggleWorker.run(
                    context = activity,
                    wallet = response.wallet,
                    appUrl = app.url,
                    enable = response.notifications
                )
            }

            JsonBuilder.connectEventSuccess(
                wallet = response.wallet,
                proof = response.proof,
                proofError = response.proofError,
                activity.appVersionName
            )
        } catch (e: CancellationException) {
            JsonBuilder.connectEventError(BridgeError.userDeclinedTransaction())
        } catch (e: ManifestException) {
            if (e is ManifestException.NotFound) {
                JsonBuilder.connectEventError(BridgeError.appManifestNotFound())
            } else {
                JsonBuilder.connectEventError(BridgeError.appManifestContentError())
            }
        } catch (e: Throwable) {
            FirebaseCrashlytics.getInstance().recordException(e)
            JsonBuilder.connectEventError(BridgeError.unknown(e.bestMessage))
        }
    }

    suspend fun isScam(context: Context, wallet: WalletEntity, vararg uris: Uri): Boolean {
        if (settingsRepository.isSafeModeEnabled(api) && safeModeClient.isHasScamUris(*uris)) {
            withContext(Dispatchers.Main) {
                TonConnectSafeModeDialog(context).show(wallet)
            }
            return true
        }
        return false
    }

    private suspend fun readManifest(url: String): AppEntity {
        return fetchManifest(url)
    }

    private suspend fun fetchManifest(url: String): AppEntity {
        val headers = ArrayMap<String, String>().apply {
            set("Connection", "close")
        }
        val response = api.defaultHttpClient.simple(url, headers)
        if (response.code != 200) {
            throw ManifestException.NotFound(response.code)
        }
        val body = response.body?.string() ?: throw ManifestException.FailedParse(NullPointerException())
        try {
            val app = AppEntity(body)
            dAppsRepository.insertApp(app)
            return app
        } catch (e: Throwable) {
            throw ManifestException.FailedParse(e)
        }
    }

    companion object {
        private const val TONCONNECT_PREFIX = "tonkeeper://ton-connect"

        private val othersPrefix = listOf("tc://", "https://app.tonkeeper.com/ton-connect")

        fun isTonConnectDeepLink(
            uri: Uri
        ): Boolean {
            return uri.scheme?.lowercase() == "tc" || uri.path?.lowercase() == "/ton-connect" || uri.host?.lowercase() == "ton-connect"
        }

        private fun normalizeUri(uri: Uri): Uri {
            val value = uri.toString()
            for (prefix in othersPrefix) {
                if (value.startsWith(prefix, ignoreCase = true)) {
                    return value.replace(prefix, TONCONNECT_PREFIX, ignoreCase = true).toUri()
                }
            }
            return uri
        }

    }
}