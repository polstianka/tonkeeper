package com.tonapps.tonkeeper.ui.base

import android.app.Application
import android.net.Uri
import android.util.Log
import android.webkit.WebResourceRequest
import androidx.annotation.LayoutRes
import androidx.camera.core.imagecapture.BundlingNode
import androidx.core.net.toUri
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.tonapps.extensions.appVersionName
import com.tonapps.extensions.bestMessage
import com.tonapps.extensions.filterList
import com.tonapps.tonkeeper.deeplink.DeepLink
import com.tonapps.tonkeeper.deeplink.DeepLinkRoute
import com.tonapps.tonkeeper.extensions.normalizeTONSites
import com.tonapps.tonkeeper.extensions.toast
import com.tonapps.tonkeeper.manager.tonconnect.ConnectRequest
import com.tonapps.tonkeeper.manager.tonconnect.TonConnect
import com.tonapps.tonkeeper.manager.tonconnect.TonConnectManager
import com.tonapps.tonkeeper.manager.tonconnect.bridge.BridgeException
import com.tonapps.tonkeeper.manager.tonconnect.bridge.JsonBuilder
import com.tonapps.tonkeeper.manager.tonconnect.bridge.model.BridgeError
import com.tonapps.tonkeeper.manager.tonconnect.bridge.model.BridgeEvent
import com.tonapps.tonkeeper.manager.tonconnect.bridge.model.BridgeMethod
import com.tonapps.tonkeeper.ui.component.TonConnectWebView
import com.tonapps.tonkeeper.ui.screen.root.RootViewModel
import com.tonapps.tonkeeper.ui.screen.send.transaction.SendTransactionScreen
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.core.entity.SignRequestEntity
import com.tonapps.wallet.data.dapps.entities.AppConnectEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import uikit.base.BaseFragment
import uikit.extensions.activity
import java.util.concurrent.CancellationException

abstract class InjectedTonConnectScreen(@LayoutRes layoutId: Int, wallet: WalletEntity): WalletContextScreen(layoutId, wallet) {

    private val tonConnectManager: TonConnectManager by inject()
    private val api: API by inject()
    private val rootViewModel: RootViewModel by activityViewModel()

    abstract var webView: TonConnectWebView

    abstract val startUri: Uri

    private val uri: Uri
        get() = webView.url?.toUri() ?: startUri

    val deviceInfo: JSONObject by lazy {
        JsonBuilder.device(wallet.maxMessages, requireContext().appVersionName)
    }

    val installId: String
        get() = rootViewModel.installId

    fun back() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            finish()
        }
    }

    override fun onBackPressed(): Boolean {
        back()
        return false
    }

    fun overrideUrlLoading(request: WebResourceRequest): Boolean {
        val refererUri = request.requestHeaders?.get("Referer")?.toUri()
        val url = request.url.normalizeTONSites()
        val scheme = url.scheme ?: ""
        if (scheme == "https" && url.host != "app.tonkeeper.com") {
            return false
        }
        val deeplink = DeepLink(url, false, refererUri)
        return processDeeplink(deeplink, url.toString())
    }

    fun processDeeplink(deeplink: DeepLink, url: String): Boolean {
        return when (deeplink.route) {
            is DeepLinkRoute.TonConnect -> {
                rootViewModel.processTonConnectDeepLink(deeplink, null)
                true
            }
            is DeepLinkRoute.Unknown -> {
                navigation?.openURL(url)
                true
            }
            else -> rootViewModel.processDeepLink(uri = url.toUri(), fromQR = false, refSource = deeplink.referrer, internal = false, fromPackageName = null)
        }
    }

    suspend fun tonapiFetch(
        url: String,
        options: String
    ) = api.tonapiFetch(url, options)

    suspend fun tonconnect(
        version: Int,
        request: ConnectRequest
    ): JSONObject {
        if (version != 2) {
            return JsonBuilder.connectEventError(BridgeError.badRequest("Version $version is not supported"))
        }
        val activity = requireContext().activity ?: return JsonBuilder.connectEventError(BridgeError.unknown("internal client error"))
        if (tonConnectManager.isScam(requireContext(), wallet, request.manifestUrl.toUri(), webView.url!!.toUri(), startUri)) {
            return JsonBuilder.connectEventError(BridgeError.unknown("internal client error"))
        }

        return tonConnectManager.launchConnectFlow(
            activity = activity,
            tonConnect = TonConnect.fromJsInject(request, webView.url?.toUri()),
            wallet = wallet
        )
    }

    suspend fun tonconnectSend(array: JSONArray): JSONObject {
        var id = 0L
        try {
            val messages = BridgeEvent.Message.parse(array)
            if (messages.size == 1) {
                val message = messages.first()
                id = message.id
                if (message.method != BridgeMethod.SEND_TRANSACTION) {
                    return JsonBuilder.responseError(id, BridgeError.methodNotSupported("Method \"${message.method}\" not supported."))
                }
                val signRequests = message.params.map { SignRequestEntity(it, uri) }
                if (signRequests.size != 1) {
                    return JsonBuilder.responseError(id, BridgeError.badRequest("Request contains excess transactions. Required: 1, Provided: ${signRequests.size}"))
                }
                val signRequest = signRequests.first()
                return try {
                    val boc = SendTransactionScreen.run(requireContext(), wallet, signRequest)
                    JsonBuilder.responseSendTransaction(id, boc)
                } catch (e: CancellationException) {
                    JsonBuilder.responseError(id, BridgeError.userDeclinedTransaction())
                } catch (e: BridgeException) {
                    JsonBuilder.responseError(id, BridgeError.badRequest(e.bestMessage))
                } catch (e: Throwable) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    JsonBuilder.responseError(id, BridgeError.unknown(e.bestMessage))
                }
            } else {
                return JsonBuilder.responseError(id, BridgeError.badRequest("Request contains excess messages. Required: 1, Provided: ${messages.size}"))
            }
        } catch (e: Throwable) {
            navigation?.toast(e.bestMessage)
            FirebaseCrashlytics.getInstance().recordException(e)
            return JsonBuilder.responseError(id, BridgeError.unknown(e.bestMessage))
        }
    }

    abstract class ViewModel(
        app: Application,
        private val wallet: WalletEntity,
        private val tonConnectManager: TonConnectManager
    ): BaseWalletVM(app) {

        abstract val url: Uri

        val connectionFlow = tonConnectManager.walletConnectionsFlow(wallet).filterList { connection ->
            connection.type == AppConnectEntity.Type.Internal && connection.appUrl.host == url.host
        }.map {
            it.firstOrNull()
        }

        fun disconnect() {
            tonConnectManager.disconnect(wallet, url, AppConnectEntity.Type.Internal)
        }

        open suspend fun restoreConnection(currentUri: Uri?): JSONObject {
            val connection = loadConnection(currentUri = currentUri)
            return if (connection == null) {
                JsonBuilder.connectEventError(BridgeError.unknownApp())
            } else {
                JsonBuilder.connectEventSuccess(wallet, null, null, context.appVersionName)
            }
        }

        protected suspend fun loadConnection(attempt: Int = 0, currentUri: Uri?): AppConnectEntity? {
            if (attempt > 3) {
                val firstApp = tonConnectManager.getConnection(wallet.accountId, wallet.testnet, url, AppConnectEntity.Type.Internal)
                if (firstApp != null) {
                    return firstApp
                }
                if (currentUri != null) {
                    return tonConnectManager.getConnection(wallet.accountId, wallet.testnet, currentUri, AppConnectEntity.Type.Internal)
                }
                return null
            }
            val connection = connectionFlow.firstOrNull()
            if (connection != null) {
                return connection
            }
            delay(140)
            return loadConnection(attempt + 1, currentUri)
        }
    }

}