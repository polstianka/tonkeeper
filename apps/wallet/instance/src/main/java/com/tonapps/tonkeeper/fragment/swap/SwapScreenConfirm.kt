package com.tonapps.tonkeeper.fragment.swap

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.tonapps.extensions.getParcelableCompat
import com.tonapps.tonkeeper.fragment.swap.model.SwapConfirmArgs
import com.tonapps.tonkeeper.sign.SignRequestEntity
import com.tonapps.tonkeeper.ui.component.swap.SwapView
import com.tonapps.tonkeeper.ui.screen.root.RootViewModel
import com.tonapps.tonkeeper.ui.screen.swap.StonfiBridge
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import uikit.base.BaseFragment
import uikit.extensions.setBottomInset
import uikit.widget.HeaderView
import uikit.widget.webview.bridge.StonfiWebView

class SwapScreenConfirm : BaseFragment(R.layout.fragment_swap_confirm), BaseFragment.BottomSheet {

    private val rootViewModel: RootViewModel by activityViewModel()

    private val headerView: HeaderView by lazy(LazyThreadSafetyMode.NONE) {
        requireView().findViewById(R.id.header)
    }

    private val swapView: SwapView by lazy(LazyThreadSafetyMode.NONE) {
        requireView().findViewById(R.id.swap)
    }

    private val webView: StonfiWebView by lazy(LazyThreadSafetyMode.NONE) {
        requireView().findViewById(R.id.hidden_web)
    }

    private val swapConfirmArgs: SwapConfirmArgs by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getParcelableCompat(KEY_SWAP_CONFIRM)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireView().findViewById<ViewGroup>(R.id.container).setBottomInset()

        initWebView()
        initHeader()
        initSwap()

        requireView().findViewById<View>(R.id.cancel).setOnClickListener {
            finish()
        }
    }

    private fun initWebView() {
        val args = swapConfirmArgs
        webView.jsBridge = StonfiBridge(
            address = args.walletAddress,
            close = ::finish,
            sendTransaction = ::sing
        )

        requireView().findViewById<View>(R.id.confirm).setOnClickListener {
            when {
                args.send.isTon && !args.receive.isTon -> {
                    webView.swapTonToJetton(
                        addressWallet = args.walletAddress,
                        askJettonAddress = args.receive.address,
                        offerAmount = args.offerAmount,
                        minAskAmount = args.minAskAmount
                    )
                }

                !args.send.isTon && args.receive.isTon -> {
                    webView.swapJettonToTon(
                        addressWallet = args.walletAddress,
                        offerJettonAddress = args.send.address,
                        offerAmount = args.offerAmount,
                        minAskAmount = args.minAskAmount
                    )
                }

                else -> {
                    webView.swapJettonToJetton(
                        addressWallet = args.walletAddress,
                        askJettonAddress = args.send.address,
                        offerJettonAddress = args.receive.address,
                        offerAmount = args.offerAmount,
                        minAskAmount = args.minAskAmount
                    )
                }
            }
        }
    }

    private suspend fun sing(
        request: SignRequestEntity
    ): String {
        return rootViewModel.requestSign(requireContext(), request)
    }

    private fun initSwap() {
        val args = swapConfirmArgs
        swapView.setConfirmMode(args.send, args.receive, args.simulate)

    }

    private fun initHeader() {
        headerView.contentMatchParent()
        headerView.doOnActionClick = {
            finish()
        }
    }


    companion object {

        private const val KEY_SWAP_CONFIRM = "key_swap_confirm"
        fun newInstance(swapConfirmArgs: SwapConfirmArgs) = SwapScreenConfirm().apply {
            arguments = Bundle().apply {
                putParcelable(KEY_SWAP_CONFIRM, swapConfirmArgs)
            }
        }
    }
}