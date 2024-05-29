package com.tonapps.tonkeeper.ui.screen.swap.legacy

import android.net.Uri
import android.os.Bundle
import android.view.View
import com.tonapps.tonkeeper.sign.SignRequestEntity
import com.tonapps.tonkeeper.ui.screen.root.RootViewModel
import com.tonapps.tonkeeper.ui.screen.swap.stonfi.StonfiBridge2
import com.tonapps.tonkeeperx.BuildConfig
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import uikit.base.BaseFragment
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.getDimensionPixelSize
import uikit.widget.webview.bridge.BridgeWebView

@Deprecated(message = "Migrate to the new SwapScreen")
class SwapScreenLegacy: BaseFragment(R.layout.fragment_swap_legacy), BaseFragment.BottomSheet {

    private val args: SwapArgsLegacy by lazy { SwapArgsLegacy(requireArguments()) }

    private val rootViewModel: RootViewModel by activityViewModel()

    private lateinit var webView: BridgeWebView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        builder.appendQueryParameter("clientVersion", BuildConfig.VERSION_NAME)
        builder.appendQueryParameter("ft", args.fromToken)
        args.toToken?.let {
            builder.appendQueryParameter("tt", it)
        }
        return builder.build()
    }

    private suspend fun sing(
        request: SignRequestEntity
    ): String {
        return rootViewModel.requestSign(requireContext(), request)
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
        ): SwapScreenLegacy {
            val fragment = SwapScreenLegacy()
            fragment.arguments = SwapArgsLegacy(uri, address, fromToken, toToken).toBundle()
            return fragment
        }
    }
}