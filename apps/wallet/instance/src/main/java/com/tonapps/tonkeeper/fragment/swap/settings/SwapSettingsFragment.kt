package com.tonapps.tonkeeper.fragment.swap.settings

import android.os.Bundle
import android.view.View
import com.tonapps.tonkeeper.fragment.swap.domain.model.SwapSettings
import com.tonapps.tonkeeperx.R
import core.extensions.observeFlow
import uikit.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.extensions.applyNavBottomPadding
import uikit.widget.ModalHeader

class SwapSettingsFragment : BaseFragment(R.layout.fragment_swap_settings), BaseFragment.BottomSheet {

    companion object {
        fun newInstance(settings: SwapSettings) = SwapSettingsFragment().apply {
            setArgs(
                SwapSettingsArgs(settings)
            )
        }
    }

    private val viewModel: SwapSettingsViewModel by viewModel()
    private val header: ModalHeader?
        get() = view?.findViewById(R.id.fragment_swap_settings_header)
    private val footer: View?
        get() = view?.findViewById(R.id.fragment_swap_settings_footer)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.provideArgs(
                SwapSettingsArgs(requireArguments())
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        header?.onCloseClick = { viewModel.onCloseClick() }

        footer?.applyNavBottomPadding()

        observeFlow(viewModel.events) { handleEvent(it) }
    }

    private fun handleEvent(event: SwapSettingsEvent) {
        when (event) {
            SwapSettingsEvent.NavigateBack -> finish()
        }
    }
}