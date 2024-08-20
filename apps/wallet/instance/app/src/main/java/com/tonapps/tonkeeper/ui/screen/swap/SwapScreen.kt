package com.tonapps.tonkeeper.ui.screen.swap

import android.net.Uri
import android.os.Bundle
import android.view.View
import com.tonapps.extensions.appVersionName
import com.tonapps.tonkeeper.core.AnalyticsHelper
import com.tonapps.tonkeeper.ui.screen.root.RootViewModel
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.data.battery.entity.BatterySupportedTransaction
import com.tonapps.wallet.data.core.entity.SignRequestEntity
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import uikit.base.BaseFragment
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.getDimensionPixelSize
import uikit.widget.HeaderView
import uikit.widget.webview.bridge.BridgeWebView

class SwapScreen: BaseFragment(R.layout.fragment_swap), BaseFragment.BottomSheet {

    private val args: SwapArgs by lazy { SwapArgs(requireArguments()) }

    private val rootViewModel: RootViewModel by activityViewModel()

    private lateinit var headerView: HeaderView
    private lateinit var webView: BridgeWebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AnalyticsHelper.trackEvent("swap_open")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerView = view.findViewById(R.id.header)
        headerView.doOnActionClick = { finish() }

        webView = view.findViewById(R.id.web)
        webView.clipToPadding = false
        webView.applyNavBottomPadding(requireContext().getDimensionPixelSize(uikit.R.dimen.offsetMedium))
        webView.loadUrl(getUri().toString())
        webView.jsBridge = StonfiBridge2(
            address = args.address,
            close = ::finish,
            sendTransaction = ::sing
        )
    }

    private fun getUri(): Uri {
        val builder = args.uri.buildUpon()
        builder.appendQueryParameter("clientVersion", requireContext().appVersionName)
        builder.appendQueryParameter("ft", args.fromToken)
        args.toToken?.let {
            builder.appendQueryParameter("tt", it)
        }
        return builder.build()
    }

    private suspend fun sing(
        request: SignRequestEntity
    ): String {
        return rootViewModel.requestSign(requireContext(), request, BatterySupportedTransaction.SWAP)
    }

    override fun onDestroyView() {
        webView.destroy()
        super.onDestroyView()
    }

    companion object {

        fun newInstance(
            uri: Uri,
            address: String,
            fromToken: String,
            toToken: String? = null
        ): SwapScreen {
            val fragment = SwapScreen()
            fragment.setArgs(SwapArgs(uri, address, fromToken, toToken))
            return fragment
        }
    }
}