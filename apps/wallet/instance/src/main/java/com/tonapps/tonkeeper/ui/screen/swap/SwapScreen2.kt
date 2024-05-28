package com.tonapps.tonkeeper.ui.screen.swap

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.transition.TransitionManager
import com.tonapps.tonkeeper.ui.screen.buysell.FiatAmountScreen
import com.tonapps.tonkeeper.ui.screen.root.RootViewModel
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView

class SwapScreen2 : BaseFragment(R.layout.fragment_swap_2), BaseFragment.BottomSheet {

    private val swapViewModel: SwapViewModel by viewModel()
    private val rootViewModel: RootViewModel by activityViewModel()

    private lateinit var headerView: HeaderView
    private lateinit var swapView: SwapView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        headerView = view.findViewById(R.id.header)
        headerView.doOnActionClick = { finish() }
        headerView.doOnCloseClick = { navigation?.add(SwapSettingsScreen.newInstance()) }

        swapView = view.findViewById(R.id.swap_view_1)
        swapView.setOnSendTokenClickListener {
            navigation?.add(WalletAssetsPickerScreen.newInstance(true, it?.token?.symbol.orEmpty()))
        }
        swapView.setOnReceiveTokenClickListener {
            navigation?.add(
                WalletAssetsPickerScreen.newInstance(
                    false,
                    it?.token?.symbol.orEmpty()
                )
            )
        }
        swapView.addSendTextChangeListener(swapViewModel::onSendTextChange)
        swapView.addReceiveTextChangeListener(swapViewModel::onReceiveTextChange)
        swapView.setOnSwapClickListener { swapViewModel.swap() }
        swapView.doOnClick = {
            when (it) {
                SwapUiModel.BottomButtonState.Continue -> swapViewModel.onContinueClick()
                SwapUiModel.BottomButtonState.Insufficient -> navigation?.add(FiatAmountScreen.newInstance())
                SwapUiModel.BottomButtonState.Confirm -> swapViewModel.onConfirmClick()
                else -> {}
            }
        }
        swapView.doOnCancel = { swapViewModel.onCancelClick() }

        collectFlow(swapViewModel.uiModel) {
            if (!it.confirmState) {
                swapView.setSendToken(it.sendToken)
                swapView.setReceiveToken(it.receiveToken)
            }
            swapView.updateBottomButton(it.bottomButtonState)
            swapView.setSendText(it.sendInput)
            swapView.setReceivedText(it.receiveInput)
            swapView.setDetails(it.details)
            swapView.setConfirmState(it.confirmState)
            val headerText = if (it.confirmState) {
                com.tonapps.wallet.localization.R.string.confirm_swap
            } else {
                com.tonapps.wallet.localization.R.string.swap
            }
            headerView.titleView.setText(headerText)
            headerView.closeView.isVisible = !it.confirmState
            TransitionManager.beginDelayedTransition(headerView)
        }

        collectFlow(swapViewModel.signRequestEntity) {
            it?.let {
                rootViewModel.requestSign(requireContext(), it)
            }
        }
    }
}