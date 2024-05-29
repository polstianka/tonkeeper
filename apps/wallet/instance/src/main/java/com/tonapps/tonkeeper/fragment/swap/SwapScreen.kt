package com.tonapps.tonkeeper.fragment.swap

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.animation.doOnEnd
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.tonapps.extensions.getParcelableCompat
import com.tonapps.tonkeeper.fragment.swap.assets.AssetsScreen
import com.tonapps.tonkeeper.fragment.swap.model.Slippage
import com.tonapps.tonkeeper.fragment.swap.model.SwapButtonState
import com.tonapps.tonkeeper.fragment.swap.model.SwapTarget
import com.tonapps.tonkeeper.sign.SignRequestEntity
import com.tonapps.tonkeeper.ui.component.swap.SwapView
import com.tonapps.tonkeeper.ui.screen.root.RootViewModel
import com.tonapps.tonkeeper.ui.screen.swap.StonfiBridge
import com.tonapps.tonkeeperx.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.extensions.setBottomInset
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView
import uikit.widget.LoaderView
import uikit.widget.SkeletonLayout
import uikit.widget.SnackView
import uikit.widget.webview.bridge.BridgeWebView
import uikit.widget.webview.bridge.StonfiWebView
import kotlin.coroutines.suspendCoroutine

class SwapScreen : BaseFragment(R.layout.fragment_swap), BaseFragment.BottomSheet {

    private val swapViewMode: SwapViewModel by activityViewModel()
    private val rootViewModel: RootViewModel by activityViewModel()

    private val shimmer: SkeletonLayout by lazy(LazyThreadSafetyMode.NONE) {
        requireView().findViewById(R.id.shimmer)
    }

    private val headerView: HeaderView by lazy(LazyThreadSafetyMode.NONE) {
        requireView().findViewById(R.id.header)
    }

    private val swapView: SwapView by lazy(LazyThreadSafetyMode.NONE) {
        requireView().findViewById(R.id.swap)
    }

    private val webView: StonfiWebView by lazy(LazyThreadSafetyMode.NONE) {
        requireView().findViewById(R.id.web)
    }

    private val confirmBtnView: Button by lazy(LazyThreadSafetyMode.NONE) {
        requireView().findViewById(R.id.confirm)
    }

    private val confirmLoadingView: LoaderView by lazy(LazyThreadSafetyMode.NONE) {
        requireView().findViewById(R.id.confirm_loading)
    }

    private val snackView: SnackView by lazy(LazyThreadSafetyMode.NONE) {
        requireView().findViewById(R.id.snack)
    }

    private var initContent: (() -> Unit)? = {
        animateLoadingContentEnd()
        initListeners()
    }

    private val address by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getString(KEY_ADDRESS)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireView().findViewById<ViewGroup>(R.id.container).setBottomInset()

        if (savedInstanceState == null) {
            swapViewMode.resetState()
        }
        initWebView()
        initHeader()
        initSwap()
    }

    private fun initWebView() {
        webView.jsBridge = StonfiBridge(
            address = address,
            close = ::finish,
            sendTransaction = ::sing
        )
    }

    private suspend fun sing(
        request: SignRequestEntity
    ): String {
        return rootViewModel.requestSign(requireContext(), request)
    }

    private fun initSwap() {
        collectFlow(swapViewMode.stateLoadingFlow) { isLoading ->
            swapView.receiveView.isLoading = isLoading
        }
        collectFlow(swapViewMode.buttonStateFlow) { swapButtonState ->
            setButtonState(swapButtonState)
        }

        collectFlow(swapViewMode.stateFlow) { state ->
            initContent?.invoke().also {
                initContent = null
            }
            swapView.sendView.setTokenState(state.send)
            swapView.receiveView.setTokenState(state.receive)
        }

        collectFlow(swapViewMode.simulateFlow) {
            swapView.receiveView.simulate = it
        }

        navigation?.setFragmentResultListener(SlippageScreen.SLIPPAGE_REQUEST) {
            it.getParcelableCompat<Slippage>(SlippageScreen.KEY_SLIPPAGE)?.let { slippage ->
                swapViewMode.onSlippageChange(slippage)
            }
        }
    }

    private fun initHeader() {
        headerView.contentMatchParent()
        headerView.doOnCloseClick = {
            navigation?.add(SlippageScreen.newInstance(swapViewMode.slippageFlow.value))
        }
        headerView.doOnActionClick = {
            finish()
        }
    }

    private fun initListeners() {
        confirmBtnView.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO){
                val args = swapViewMode.getSwapArgs() ?:return@launch

                withContext(Dispatchers.Main){
                    navigation?.add(SwapScreenConfirm.newInstance(args.copy(walletAddress = address)))
                }
            }
        }
        swapView.sendView.onTokenClick = {
            navigation?.add(AssetsScreen.newInstance(SwapTarget.Send))
        }
        swapView.receiveView.onTokenClick = {
            navigation?.add(AssetsScreen.newInstance(SwapTarget.Receive))
        }
        swapView.sendView.onMaxClick = {
            swapViewMode.onClickSendMax()
        }

        swapView.sendView.onAmountChange = {
            swapViewMode.onChangeValue(it)
        }

        swapView.onSwapClick = {
            lifecycleScope.launch {
                val state = swapViewMode.stateFlow.firstOrNull() ?:return@launch
                if (state.receive != null) {
                    swapView.btnSwapView.animateMove()
                    swapViewMode.onClickSwap()
                }
            }
        }
        swapView.receiveView.onSnackShow = {
            snackView.show(it)
        }
    }

    private fun setButtonState(buttonState: SwapButtonState) {
        confirmBtnView.setBackgroundResource(buttonState.backgroundRes)
        if (buttonState.textRes != null) {
            confirmBtnView.setText(buttonState.textRes)
        } else {
            confirmBtnView.text = ""
        }
        confirmBtnView.setTextColor(buttonState.textColor(requireContext()))
        confirmLoadingView.isVisible = buttonState.showLoading

        confirmBtnView.isEnabled = buttonState == SwapButtonState.Confirm
    }

    private fun animateLoadingContentEnd() {
        val animationSet = AnimatorSet()
        animationSet.duration = 180

        val swapContentViews = swapView.getContentViews()

        val animations = swapContentViews.filter { !(it.isVisible && it.alpha == 1.0f) }.map {
            it.visibility = View.VISIBLE
            ObjectAnimator.ofFloat(it, View.ALPHA, it.alpha, 1f)
        }.toMutableList<Animator>()

        val swapAnimator = ObjectAnimator.ofFloat(swapView, View.ALPHA, swapView.alpha, 1f)
        animations.add(swapAnimator)

        val shimmerAnimator = ObjectAnimator.ofFloat(shimmer, View.ALPHA, shimmer.alpha, 0f)
        animations.add(shimmerAnimator)

        val buttonAnimator = ObjectAnimator.ofFloat(confirmBtnView, View.ALPHA, confirmBtnView.alpha, 1f)
        animations.add(buttonAnimator)

        setButtonState(SwapButtonState.EnterAmount)

        animationSet.doOnEnd {
            shimmer.isGone = true
        }
        animationSet.playTogether(animations)
        animationSet.start()
    }

    override fun onEndShowingAnimation() {

    }

    override fun onDragging() {

    }


    companion object {
        private const val KEY_ADDRESS = "address"
        fun newInstance(address: String) = SwapScreen().apply {
            arguments = Bundle().apply {
                putString(KEY_ADDRESS, address)
            }
        }
    }
}