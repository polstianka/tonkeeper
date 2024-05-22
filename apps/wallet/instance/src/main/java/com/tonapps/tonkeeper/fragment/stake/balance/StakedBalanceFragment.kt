package com.tonapps.tonkeeper.fragment.stake.balance

import android.os.Bundle
import android.view.View
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakedBalance
import com.tonapps.tonkeeperx.R
import core.extensions.observeFlow
import uikit.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.widget.HeaderView

class StakedBalanceFragment : BaseFragment(
    R.layout.fragment_staked_balance
), BaseFragment.SwipeBack {

    companion object {
        fun newInstance(
            stakedBalance: StakedBalance
        ) = StakedBalanceFragment().apply {
            setArgs(
                StakedBalanceArgs(stakedBalance)
            )
        }
    }

    private val viewModel: StakedBalanceViewModel by viewModel()
    private val header: HeaderView?
        get() = view?.findViewById(R.id.fragment_staked_balance_header)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.provideArgs(
                StakedBalanceArgs(
                    requireArguments()
                )
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        header?.doOnCloseClick = { viewModel.onCloseClicked() }

        observeFlow(viewModel.events) { handleEvent(it) }
        observeFlow(viewModel.args) { updateState(it) }
    }

    private fun updateState(args: StakedBalanceArgs) {
        header?.title = args.stakedBalance.pool.name
    }

    private fun handleEvent(event: StakedBalanceEvent) {
        when (event) {
            StakedBalanceEvent.NavigateBack -> finish()
        }
    }
}