package com.tonapps.tonkeeper.ui.screen.swap

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.tonapps.tonkeeper.sign.SignRequestEntity
import com.tonapps.tonkeeper.ui.component.AssetView
import com.tonapps.tonkeeper.ui.screen.root.RootViewModel
import com.tonapps.tonkeeper.ui.screen.swap.choosetoken.ChooseTokenScreen
import com.tonapps.tonkeeper.ui.screen.swap.settings.SwapSettingsScreen
import com.tonapps.tonkeeperx.BuildConfig
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.getDimensionPixelSize
import uikit.navigation.Navigation
import uikit.widget.HeaderView
import uikit.widget.webview.bridge.BridgeWebView

class SwapScreen : BaseFragment(R.layout.fragment_swap), BaseFragment.BottomSheet {
    private val args: SwapArgs by lazy { SwapArgs(requireArguments()) }

    private val rootViewModel: RootViewModel by activityViewModel()
    private val swapViewMode: SwapViewModel by viewModel()

    private lateinit var headerView: HeaderView
    private lateinit var webView: BridgeWebView
    private lateinit var sendAssetView: AssetView
    private lateinit var receiveAssetView: AssetView

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        headerView = view.findViewById(R.id.header)
        headerView.doOnActionClick = { finish() }
        headerView.doOnCloseClick = { Navigation.from(requireContext())?.add(SwapSettingsScreen()) }
        webView = view.findViewById(R.id.web)
        webView.clipToPadding = false
        webView.applyNavBottomPadding(requireContext().getDimensionPixelSize(uikit.R.dimen.offsetMedium))
        webView.loadUrl(getUri().toString())
        webView.jsBridge =
            StonfiBridge2(
                address = args.address,
                close = ::finish,
                sendTransaction = ::sing,
            )
        sendAssetView =
            view.findViewById<ConstraintLayout>(R.id.send_layout).findViewById(R.id.asset_layout)
        receiveAssetView =
            view.findViewById<ConstraintLayout>(R.id.receive_layout).findViewById(R.id.asset_layout)
        sendAssetView.setOnClickListener {
            Navigation.from(requireContext())?.add(ChooseTokenScreen())
        }
        receiveAssetView.setOnClickListener {
            Navigation.from(requireContext())?.add(ChooseTokenScreen())
        }
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

    private suspend fun sing(request: SignRequestEntity): String {
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
            toToken: String? = null,
        ): SwapScreen {
            val fragment = SwapScreen()
            fragment.arguments = SwapArgs(uri, address, fromToken, toToken).toBundle()
            return fragment
        }
    }
}
