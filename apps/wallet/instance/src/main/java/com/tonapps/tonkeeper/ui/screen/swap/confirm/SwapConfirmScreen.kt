package com.tonapps.tonkeeper.ui.screen.swap.confirm

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import com.tonapps.tonkeeper.api.swap.StonfiSwapAsset
import com.tonapps.tonkeeper.api.swap.StonfiSwapHelper
import com.tonapps.tonkeeper.extensions.toast
import com.tonapps.tonkeeper.fragment.send.view.AmountInput
import com.tonapps.tonkeeper.sign.SignRequestEntity
import com.tonapps.tonkeeper.ui.screen.root.RootViewModel
import com.tonapps.tonkeeper.ui.screen.swap.SwapChooseView
import com.tonapps.tonkeeper.ui.screen.swap.SwapScreenEffect
import com.tonapps.tonkeeper.ui.screen.swap.SwapSimulationView
import com.tonapps.tonkeeper.ui.screen.swap.pager.PagerScreen
import com.tonapps.tonkeeper.ui.screen.swap.pager.SwapScreenAdapter
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.webview.bridge.BridgeWebView

class SwapConfirmScreen : PagerScreen<SwapConfirmScreenState, SwapConfirmScreenEffect, SwapConfirmScreenFeature>(R.layout.fragment_swap_confirm), BaseFragment.CustomBackground {

    companion object {
        fun newInstance() = SwapConfirmScreen()
    }

    override val feature: SwapConfirmScreenFeature by viewModel()

    private lateinit var tokenView: SwapChooseView
    private lateinit var tokenViewRec: SwapChooseView
    private lateinit var valueView: AmountInput
    private lateinit var valueRecView: AmountInput
    private lateinit var availableView: AppCompatTextView
    private lateinit var availableViewRec: AppCompatTextView
    private lateinit var continueButton: FrameLayout
    private lateinit var cancelButton: FrameLayout
    private lateinit var simulationView: SwapSimulationView
    private lateinit var bridgeWebView: BridgeWebView
    private lateinit var close: View
    private lateinit var loader: View
    private lateinit var continueText: View
    private val rootViewModel: RootViewModel by activityViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        continueText = view.findViewById(R.id.button_text)
        loader = view.findViewById(R.id.loader)
        simulationView = view.findViewById(R.id.simulation)
        simulationView.setHintListener {
            navigation?.toast(it)
        }
        tokenView = view.findViewById(R.id.token)
        tokenView.isEnabled = false
        bridgeWebView = view.findViewById(R.id.bridgeWebView)

        close = view.findViewById(R.id.close)
        close.setOnClickListener {
            swapFeature.setCurrentPage(SwapScreenAdapter.POSITION_AMOUNT)
        }

        tokenViewRec = view.findViewById(R.id.token_rec)
        tokenViewRec.isEnabled = false
        valueView = view.findViewById(R.id.value)
        valueRecView = view.findViewById(R.id.value_rec)
        valueRecView.gravity = Gravity.CENTER_VERTICAL or Gravity.END
        valueView.gravity = Gravity.CENTER_VERTICAL or Gravity.END
        availableView = view.findViewById(R.id.available)
        availableViewRec = view.findViewById(R.id.available_rec)

        cancelButton = view.findViewById(R.id.button_cancel)
        cancelButton.setOnClickListener {
            swapFeature.setCurrentPage(SwapScreenAdapter.POSITION_AMOUNT)
        }
        continueButton = view.findViewById(R.id.button_continue)
        continueButton.isSelected = true
        bridgeWebView.loadUrl(StonfiSwapHelper.STONFI_SDK_PAGE)
        continueButton.setOnClickListener {
            val helper = StonfiSwapHelper(
                bridgeWebView,
                doOnClose = {
                    swapFeature.sendEffect(SwapScreenEffect.FinishAndGoHistory)
                },
                sendTransaction = ::sing
            )
            feature.swap(helper)
        }
    }

    private fun setFrom(asset: StonfiSwapAsset?) {
        if (asset != null) {
            tokenView.setData(SwapChooseView.SwapItem.Asset(asset.symbol, asset.imageURL))
        } else {
            tokenView.setData(SwapChooseView.SwapItem.Hint())
        }
    }

    private fun setTo(asset: StonfiSwapAsset?) {
        if (asset != null) {
            tokenViewRec.setData(SwapChooseView.SwapItem.Asset(asset.symbol, asset.imageURL))
        } else {
            tokenViewRec.setData(SwapChooseView.SwapItem.Hint())
        }
    }

    override fun newUiState(state: SwapConfirmScreenState) {
        valueView.setDecimalCount(state.decimals)

        valueView.setText(state.swapFromAmount)
        valueRecView.setText(state.swapToAmount)

        simulationView.visibility = View.VISIBLE
        if (state.simulateData != null && state.swapFrom != null && state.swapTo != null) {
            simulationView.setData(state.simulateData, state.swapFrom, state.swapTo, state.slippage)
        }

        setFrom(state.swapFrom)
        setTo(state.swapTo)

        availableView.text = state.available
        availableViewRec.text = state.availableRec

        cancelButton.isEnabled = state.canContinue
        continueButton.isEnabled = state.canContinue
        loader.isVisible = !state.canContinue
        continueText.isVisible = state.canContinue
    }

    private suspend fun sing(request: SignRequestEntity): String {
        return rootViewModel.requestSign(requireContext(), request)
    }

    override fun onVisibleChange(visible: Boolean) {
        super.onVisibleChange(visible)
        if (visible) {
            swapFeature.swap.value?.let {
                feature.setData(it)
            }
        }
    }
}