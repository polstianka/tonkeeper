package com.tonapps.tonkeeper.ui.screen.swap

import android.os.Bundle
import android.view.View
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView

class SwapScreen2 : BaseFragment(R.layout.fragment_swap_2), BaseFragment.BottomSheet {

    private val swapViewModel: SwapViewModel by viewModel()

    private lateinit var headerView: HeaderView
    private lateinit var swapView: SwapView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        headerView = view.findViewById(R.id.header)
        headerView.doOnActionClick = { finish() }

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

        collectFlow(swapViewModel.uiModel) {
            swapView.setSendToken(it.sendToken)
            swapView.setReceiveToken(it.receiveToken)
            swapView.updateBottomButton(it.bottomButtonState)
        }
    }
}