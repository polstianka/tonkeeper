package com.tonapps.tonkeeper.ui.screen.swap

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.ktx.performance
import com.tonapps.extensions.appVersionName
import com.tonapps.extensions.locale
import com.tonapps.tonkeeper.core.AnalyticsHelper
import com.tonapps.tonkeeper.helper.BrowserHelper
import com.tonapps.tonkeeper.ui.base.BaseWalletVM
import com.tonapps.tonkeeper.ui.base.WalletContextScreen
import com.tonapps.tonkeeper.ui.screen.send.transaction.SendTransactionScreen
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.core.entity.SignRequestEntity
import com.tonapps.wallet.data.settings.BatteryTransaction
import com.tonapps.wallet.data.settings.SettingsRepository
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.getDimensionPixelSize
import uikit.widget.webview.WebViewFixed
import uikit.widget.webview.bridge.BridgeWebView

class SwapScreen(wallet: WalletEntity): WalletContextScreen(R.layout.fragment_swap, wallet), BaseFragment.BottomSheet {

    override val fragmentName: String = "SwapScreen"

    private val args: SwapArgs by lazy { SwapArgs(requireArguments()) }

    override val viewModel: BaseWalletVM.EmptyViewViewModel by viewModel()

    private val settingsRepository: SettingsRepository by inject()

    private lateinit var closeView: View
    private lateinit var webView: BridgeWebView

    private val webViewCallback = object : WebViewFixed.Callback() {

        private val swapTrace = Firebase.performance.newTrace("swap_webview")
        private var isAlreadySendTrace = false

        override fun onPageFinished(url: String) {
            super.onPageFinished(url)
            if (!isAlreadySendTrace) {
                swapTrace.stop()
                isAlreadySendTrace = true
            }
            hideCloseView()
        }

        override fun onPageStarted(url: String, favicon: Bitmap?) {
            super.onPageStarted(url, favicon)
            if (!isAlreadySendTrace) {
                swapTrace.start()
            }
        }

        override fun onNewTab(url: String) {
            super.onNewTab(url)
            BrowserHelper.open(requireContext(), url)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AnalyticsHelper.trackEvent("swap_open", settingsRepository.installId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        closeView = view.findViewById(R.id.close)
        closeView.setOnClickListener { finish() }

        webView = view.findViewById(R.id.web)
        webView.addCallback(webViewCallback)
        webView.clipToPadding = false
        webView.applyNavBottomPadding(requireContext().getDimensionPixelSize(uikit.R.dimen.offsetMedium))
        webView.loadUrl(getUri().toString())
        webView.jsBridge = StonfiBridge2(
            address = args.address,
            close = ::finish,
            sendTransaction = ::sing
        )

        ViewCompat.setOnApplyWindowInsetsListener(webView) { _, insets ->
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            webView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = navInsets.bottom
            }
            insets
        }
    }

    private fun hideCloseView() {
        if (closeView.visibility == View.GONE) {
            return
        }
        closeView.postDelayed({
            closeView.visibility = View.GONE
        }, 1000)
    }

    private fun getUri(): Uri {
        val builder = args.uri.buildUpon()
        builder.appendQueryParameter("clientVersion", requireContext().appVersionName)
        builder.appendQueryParameter("ft", args.fromToken)
        builder.appendQueryParameter("lang", requireContext().locale.toString())
        args.toToken?.let {
            builder.appendQueryParameter("tt", it)
        }
        val theme = settingsRepository.theme
        builder.appendQueryParameter("theme", if (theme.isSystem) "dark" else theme.key)
        return builder.build()
    }

    private suspend fun sing(
        request: SignRequestEntity
    ): String {
        return try {
            SendTransactionScreen.run(requireContext(), wallet, request, BatteryTransaction.SWAP)
        } catch (e: Throwable) {
            ""
        }
    }

    override fun onDestroyView() {
        webView.removeCallback(webViewCallback)
        webView.destroy()
        super.onDestroyView()
    }

    companion object {

        fun newInstance(
            wallet: WalletEntity,
            uri: Uri,
            address: String,
            fromToken: String,
            toToken: String? = null
        ): SwapScreen {
            val fragment = SwapScreen(wallet)
            fragment.setArgs(SwapArgs(uri, address, fromToken, toToken))
            return fragment
        }
    }
}