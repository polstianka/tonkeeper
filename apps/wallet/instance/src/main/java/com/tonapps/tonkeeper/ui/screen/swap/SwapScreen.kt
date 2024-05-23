package com.tonapps.tonkeeper.ui.screen.swap

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
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
    private lateinit var sendView: View
    private lateinit var sendLayout: LinearLayoutCompat
    private lateinit var sendTitle: AppCompatTextView
    private lateinit var sendImage: AppCompatImageView
    private var navigation: Navigation? = null

    private var slippage = 0.1f

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sendLayout = view.findViewById(R.id.button_layout)
        sendTitle = sendLayout.findViewById(R.id.title)
        sendImage = sendLayout.findViewById(R.id.icon)

        headerView = view.findViewById(R.id.header)
        navigation = Navigation.from(view.context)
        headerView.doOnCloseClick = {
            val requestKey = "sign_request"
            navigation?.setFragmentResultListener(requestKey) { bundle ->
               if(bundle.containsKey("reply")){
                   slippage = bundle.getFloat("reply")
                   Log.d("slippage", slippage.toString())
               }
            }
            navigation?.add(SlippageScreen.newInstance(requestKey))
        }
        headerView.doOnActionClick = { this.finish() }
        headerView.clipToPadding = false
        headerView.applyNavBottomPadding(requireContext().getDimensionPixelSize(uikit.R.dimen.offsetExtraExtraSmall))
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