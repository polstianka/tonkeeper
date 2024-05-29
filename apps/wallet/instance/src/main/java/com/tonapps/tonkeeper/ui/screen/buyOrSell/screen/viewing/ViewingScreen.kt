package com.tonapps.tonkeeper.ui.screen.buyOrSell.screen.viewing

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import com.tonapps.tonkeeper.ui.screen.swap.StonfiBridge2
import com.tonapps.tonkeeper.ui.screen.swap.SwapArgs
import com.tonapps.tonkeeper.ui.screen.swap.SwapScreen
import com.tonapps.tonkeeperx.R
import uikit.base.BaseFragment
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.getDimensionPixelSize
import uikit.widget.HeaderView
import uikit.widget.webview.bridge.BridgeWebView

class ViewingScreen: BaseFragment(R.layout.fragment_viewing), BaseFragment.BottomSheet{

    private val args: ViewingArgs by lazy { ViewingArgs(requireArguments()) }
    private lateinit var webView: BridgeWebView
    private lateinit var headerView: HeaderView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerView = view.findViewById(R.id.header)
        webView = view.findViewById(R.id.web)

        headerView.doOnActionClick = {
            finish()
        }
        headerView.title = args.upBarTitle
        webView.clipToPadding = false
        webView.settings.javaScriptEnabled = true
        webView.applyNavBottomPadding(requireContext().getDimensionPixelSize(uikit.R.dimen.offsetMedium))
        webView.loadUrl(args.urlLoad)
    }


    override fun onDestroyView() {
        webView.destroy()
        super.onDestroyView()
    }
    companion object {

        fun newInstance(
            urlLoad: String,
            upBarTitle: String
        ): ViewingScreen {
            val fragment = ViewingScreen()
            fragment.arguments = ViewingArgs(urlLoad = urlLoad, upBarTitle = upBarTitle).toBundle()
            return fragment
        }
    }

}