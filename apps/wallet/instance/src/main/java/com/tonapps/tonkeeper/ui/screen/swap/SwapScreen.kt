package com.tonapps.tonkeeper.ui.screen.swap

import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.lifecycleScope
import com.tonapps.tonkeeper.koin.koin
import com.tonapps.tonkeeper.sign.SignRequestEntity
import com.tonapps.tonkeeper.ui.screen.root.RootViewModel
import com.tonapps.tonkeeper.ui.screen.settings.slippage.SlippageScreen
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.data.token.TokenRepository
import kotlinx.coroutines.launch
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
    private lateinit var viewModel: SwapViewModel
    private lateinit var headerView: HeaderView
    private lateinit var balanceView: AppCompatTextView
    private lateinit var sendTitle: AppCompatTextView
    private lateinit var sendImage: AppCompatImageView
    private lateinit var receiveTitle: AppCompatTextView
    private lateinit var receiveImage: AppCompatImageView
    private lateinit var swapTokenButton: AppCompatImageView
    private var navigation: Navigation? = null

    private var slippage = 0.1f

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = SwapViewModel(SwapViewState(args.fromToken, args.toToken,null, null, "0", "0", "Choose Token", null), context?.koin?.get<TokenRepository>(), lifecycleScope)
        balanceView = view.findViewById(R.id.balance)
        sendTitle = view.findViewById(R.id.title)
        sendImage = view.findViewById(R.id.icon)
        receiveTitle = view.findViewById(R.id.choose_title)
        receiveImage = view.findViewById(R.id.choose_icon)
        swapTokenButton = view.findViewById(R.id.swap_token_button)
        headerView = view.findViewById(R.id.header)
        navigation = Navigation.from(view.context)
        headerView.doOnCloseClick = {
            val requestKey = "sign_request"
            navigation?.setFragmentResultListener(requestKey) { bundle ->
               if(bundle.containsKey("reply")){
                   slippage = bundle.getFloat("reply")
               }
            }
            navigation?.add(SlippageScreen.newInstance(requestKey))
        }
        headerView.doOnActionClick = { this.finish() }
        headerView.clipToPadding = false
        headerView.applyNavBottomPadding(requireContext().getDimensionPixelSize(uikit.R.dimen.offsetExtraExtraSmall))
        lifecycleScope.launch {
            viewModel.stateFlow.collect{
                balanceView.text = "Balance: ${it.balance}"
                updateTokenView(
                    it.fromTokenTitle,
                    it.toTokenTitle)
            }
        }
        swapTokenButton.setOnClickListener {
            viewModel.swapTokens()
        }
    }

//    private fun getUri(): Uri {
//        val builder = args.uri.buildUpon()
//        builder.appendQueryParameter("clientVersion", BuildConfig.VERSION_NAME)
//        builder.appendQueryParameter("ft", args.fromToken)
//        args.toToken?.let {
//            builder.appendQueryParameter("tt", it)
//        }
//        return builder.build()
//    }

    private suspend fun sing(
        request: SignRequestEntity
    ): String {
        return rootViewModel.requestSign(requireContext(), request)
    }

    private fun updateTokenView(fromToken: String?, toToken: String?){
        sendTitle.text = fromToken ?: getString(com.tonapps.wallet.localization.R.string.choose)
        if(fromToken == null){
            sendImage.setImageDrawable(null)
            sendImage.visibility = View.GONE
            setMargins(sendTitle, 12, 4, 12, 4)
        }
        else{
            sendImage.visibility = View.VISIBLE
            sendImage.setImageDrawable(getDrawable(com.tonapps.wallet.api.R.drawable.ic_ton_with_bg))
            setMargins(sendTitle, 4, 4, 12, 4)
        }
        receiveTitle.text = toToken ?: getString(com.tonapps.wallet.localization.R.string.choose)
        if(toToken == null){
            receiveImage.setImageDrawable(null)
            receiveImage.visibility = View.GONE
            setMargins(receiveTitle, 12, 4, 12, 4)
        }
        else{
            receiveImage.visibility = View.VISIBLE
            receiveImage.setImageDrawable(getDrawable(com.tonapps.wallet.api.R.drawable.ic_ton_with_bg))
            setMargins(receiveTitle, 4, 4, 12, 4)
        }
    }

    private fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun setMargins(textView: AppCompatTextView, left: Int, top: Int, right: Int, bottom: Int) {
        val leftPx = left.dpToPx()
        val topPx = top.dpToPx()
        val rightPx = right.dpToPx()
        val bottomPx = bottom.dpToPx()

        val layoutParams = textView.layoutParams
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            layoutParams.setMargins(leftPx, topPx, rightPx, bottomPx)
            textView.layoutParams = layoutParams
        } else {
            throw IllegalArgumentException("Layout parameters are not MarginLayoutParams")
        }
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