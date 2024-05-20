package com.tonapps.tonkeeper.fragment.swap.confirm

import android.os.Bundle
import android.view.View
import com.tonapps.tonkeeper.extensions.popBackToRootFragment
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAsset
import com.tonapps.tonkeeper.fragment.swap.domain.model.SwapSettings
import com.tonapps.tonkeeper.fragment.swap.root.SwapFragment
import com.tonapps.tonkeeperx.R
import core.extensions.observeFlow
import uikit.base.BaseFragment
import java.math.BigDecimal
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.widget.ModalHeader

class ConfirmSwapFragment : BaseFragment(R.layout.fragment_swap_confirm), BaseFragment.BottomSheet {

    companion object {
        fun newInstance(
            sendToken: DexAsset,
            receiveToken: DexAsset,
            amount: BigDecimal,
            settings: SwapSettings
        ) = ConfirmSwapFragment().apply {
            setArgs(
                ConfirmSwapArgs(sendToken, receiveToken, settings, amount)
            )
        }
    }

    private val viewModel: ConfirmSwapViewModel by viewModel()
    private val header: ModalHeader?
        get() = view?.findViewById(R.id.fragment_swap_confirm_header)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.provideArgs(
                ConfirmSwapArgs(requireArguments())
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        header?.onCloseClick = { viewModel.onCloseClicked() }

        observeFlow(viewModel.events) { handleEvent(it) }
    }

    private fun handleEvent(event: ConfirmSwapEvent) {
        when (event) {
            is ConfirmSwapEvent.CloseFlow -> event.handle()
        }
    }

    private fun ConfirmSwapEvent.CloseFlow.handle() {
        popBackToRootFragment(
            includingRoot = true,
            SwapFragment::class
        )
        finish()
    }
}