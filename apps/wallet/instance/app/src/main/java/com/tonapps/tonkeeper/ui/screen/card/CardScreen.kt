package com.tonapps.tonkeeper.ui.screen.card

import android.animation.ObjectAnimator
import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.tonapps.extensions.toUriOrNull
import com.tonapps.tonkeeper.koin.walletViewModel
import com.tonapps.tonkeeper.ui.base.InjectedTonConnectScreen
import com.tonapps.tonkeeper.ui.component.TonConnectWebView
import com.tonapps.tonkeeper.ui.screen.card.entity.CardBridgeEvent
import com.tonapps.tonkeeper.ui.screen.card.entity.CardScreenPath
import com.tonapps.tonkeeper.ui.screen.card.entity.CardsStateEntity
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.data.account.entities.WalletEntity
import org.koin.core.parameter.parametersOf
import uikit.base.BaseFragment
import uikit.extensions.dp
import uikit.widget.SkeletonLayout
import uikit.widget.webview.WebViewFixed

class CardScreen(
    wallet: WalletEntity,
    private val cardsState: CardsStateEntity,
    private val path: CardScreenPath
) : InjectedTonConnectScreen(R.layout.fragment_card, wallet), BaseFragment.SingleTask {

    override val fragmentName: String = "CardScreen"

    override val viewModel: CardViewModel by walletViewModel(parameters = { parametersOf(path) })

    override lateinit var webView: TonConnectWebView

    override val startUri: Uri
        get() = viewModel.url

    private val webViewCallback = object : WebViewFixed.Callback() {
        override fun shouldOverrideUrlLoading(request: WebResourceRequest): Boolean {
            return overrideUrlLoading(request)
        }

        private var firstLoading = true
        private var debounceHandler: Handler? = null
        private var debounceRunnable: Runnable? = null
        private val debounceDelay: Long = 1000

        override fun onPageFinished(url: String) {
            if (firstLoading) {

                debounceRunnable?.let { debounceHandler?.removeCallbacks(it) }

                if (debounceHandler == null) {
                    debounceHandler = Handler(Looper.getMainLooper())
                }

                debounceRunnable = Runnable {
                    firstLoading = false
                    fadeInWebView(webView)
                }

                debounceHandler?.postDelayed(debounceRunnable!!, debounceDelay)
            }
        }
    }

    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    private val fileChooserLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uriArray = if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { arrayOf(it) }
            } else null
            filePathCallback?.onReceiveValue(uriArray)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (path) {
            is CardScreenPath.Account -> {
                view.findViewById<SkeletonLayout>(R.id.shimmer_account).visibility = View.VISIBLE
            }

            is CardScreenPath.Prepaid -> {
                view.findViewById<SkeletonLayout>(R.id.shimmer_card).visibility = View.VISIBLE
            }

            is CardScreenPath.Create -> {
                view.findViewById<SkeletonLayout>(R.id.shimmer_create).visibility = View.VISIBLE
            }

            else -> {
                view.findViewById<SkeletonLayout>(R.id.shimmer_main).visibility = View.VISIBLE
            }
        }

        webView = view.findViewById(R.id.webView)

        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.allowFileAccess = true
        webView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        webView.addCallback(webViewCallback)
        webView.jsBridge = CardBridge(
            deviceInfo = deviceInfo.toString(),
            cardsState,
            send = ::tonconnectSend,
            restoreConnection = { viewModel.restoreConnection(webView.url?.toUriOrNull()) },
            onEvent = ::onEvent
        )
        webView.loadUrl(viewModel.url.toString())
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                this@CardScreen.filePathCallback = filePathCallback
                try {
                    val intent = fileChooserParams?.createIntent()
                    fileChooserLauncher.launch(intent)
                } catch (e: Exception) {
                    return false
                }
                return true
            }

            override fun onPermissionRequest(request: PermissionRequest) {
                request.grant(request.resources)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val statusInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val bottomInsets =
                insets.getInsets(WindowInsetsCompat.Type.ime() or WindowInsetsCompat.Type.navigationBars())
            val topInset = statusInsets.top + 10.dp
            view.findViewById<LinearLayoutCompat>(R.id.shimmer_container)
                .updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = topInset
                    bottomMargin = bottomInsets.bottom
                }
            applyWebViewOffset(topInset, bottomInsets.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        webView.addCallback(webViewCallback)
    }

    override fun onPause() {
        super.onPause()
        webView.removeCallback(webViewCallback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView.removeCallback(webViewCallback)
        webView.destroy()
    }

    private fun applyWebViewOffset(top: Int, bottom: Int) {
        webView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = top
            bottomMargin = bottom
        }
    }

    private fun fadeInWebView(view: View) {
        view.apply {
            alpha = 0f
            visibility = View.VISIBLE
        }
        val animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
            duration = 400
        }
        animator.start()
    }

    private fun onEvent(event: CardBridgeEvent) {
        when (event) {
            is CardBridgeEvent.CloseApp -> finish()
            is CardBridgeEvent.OpenUrl -> navigation?.openURL(event.url)
        }
    }

    companion object {

        fun newInstance(
            wallet: WalletEntity,
            cardsState: CardsStateEntity,
            path: CardScreenPath? = null
        ) =
            CardScreen(wallet, cardsState, path ?: CardScreenPath.Main)
    }
}