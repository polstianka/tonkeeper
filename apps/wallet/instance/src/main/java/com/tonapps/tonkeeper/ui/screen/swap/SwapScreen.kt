package com.tonapps.tonkeeper.ui.screen.swap

import android.net.Uri
import android.os.Bundle
import android.view.View
import com.tonapps.tonkeeper.sign.SignRequestEntity
import com.tonapps.tonkeeper.ui.screen.root.RootViewModel
import com.tonapps.tonkeeper.ui.screen.settings.slippage.SlippageScreen
import com.tonapps.tonkeeperx.BuildConfig
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import uikit.base.BaseFragment
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.getDimensionPixelSize
import uikit.navigation.Navigation
import uikit.widget.HeaderView

class SwapScreen: BaseFragment(R.layout.fragment_swap), BaseFragment.BottomSheet
{

    private val args: SwapArgs by lazy { SwapArgs(requireArguments()) }

    private val rootViewModel: RootViewModel by activityViewModel()

    private lateinit var headerView: HeaderView
    private var navigation: Navigation? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerView = view.findViewById(R.id.header)
        navigation = Navigation.from(view.context)
        headerView.doOnCloseClick = {
            navigation?.add(SlippageScreen.newInstance())
        }
        headerView.doOnActionClick = { this.finish() }
        headerView.clipToPadding = false
        headerView.applyNavBottomPadding(requireContext().getDimensionPixelSize(uikit.R.dimen.offsetExtraExtraSmall))

//        webView = view.findViewById(R.id.web)
//        webView.clipToPadding = false
//        webView.applyNavBottomPadding(requireContext().getDimensionPixelSize(uikit.R.dimen.offsetMedium))
//        webView.loadUrl(getUri().toString())
//        webView.jsBridge = StonfiBridge2(
//            address = args.address,
//            close = ::finish,
//            sendTransaction = ::sing
//        )
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

    companion object {

        fun newInstance(
            uri: Uri,
            address: String,
            fromToken: String,
            toToken: String? = null
        ): SwapScreen {
            val fragment = SwapScreen()
            fragment.arguments = SwapArgs(uri, address, fromToken, toToken).toBundle()
            return fragment
        }
    }
}