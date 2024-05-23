package com.tonapps.tonkeeper.fragment.stake.unstake

import android.os.Bundle
import android.view.View
import android.widget.Button
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakedBalance
import com.tonapps.tonkeeperx.R
import core.extensions.observeFlow
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.applyBottomInsets
import uikit.widget.HeaderView

class UnstakeFragment : BaseFragment(R.layout.fragment_unstake), BaseFragment.BottomSheet {

    companion object {
        fun newInstance(balance: StakedBalance) = UnstakeFragment().apply {
            setArgs(
                UnstakeArgs(balance)
            )
        }
    }

    private val viewModel: UnstakeViewModel by viewModel()
    private val header: HeaderView?
        get() = view?.findViewById(R.id.fragment_unstake_header)
    private val button: Button?
        get() = view?.findViewById(R.id.fragment_unstake_button)
    private val footer: View?
        get() = view?.findViewById(R.id.fragment_unstake_footer)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.provideArgs(
                UnstakeArgs(requireArguments())
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        header?.doOnActionClick = { viewModel.onCloseClicked() }

        footer?.applyBottomInsets()

        observeFlow(viewModel.events) { handleEvents(it) }
    }

    private fun handleEvents(event: UnstakeEvent) {
        when (event) {
            UnstakeEvent.NavigateBack -> finish()
        }
    }
}