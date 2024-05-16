package com.tonapps.tonkeeper.fragment.stake.confirm

import android.os.Bundle
import android.view.View
import com.tonapps.tonkeeper.extensions.popBackToRootFragment
import com.tonapps.tonkeeper.fragment.stake.domain.StakingTransactionType
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.root.StakeFragment
import com.tonapps.tonkeeperx.R
import core.extensions.observeFlow
import uikit.base.BaseFragment
import uikit.widget.HeaderView
import org.koin.androidx.viewmodel.ext.android.viewModel

class ConfirmStakeFragment : BaseFragment(R.layout.fragment_confirm_stake), BaseFragment.BottomSheet {

    companion object {
        fun newInstance(
            pool: StakingPool,
            amount: Float,
            type: StakingTransactionType
        ) = ConfirmStakeFragment().apply {
            setArgs(
                ConfirmStakeArgs(pool, amount, type)
            )
        }
    }

    private val viewModel: ConfirmStakeViewModel by viewModel()
    private val header: HeaderView?
        get() = view?.findViewById(R.id.fragment_confirm_stake_header)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.provideArgs(
                ConfirmStakeArgs(
                    requireArguments()
                )
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        header?.doOnActionClick = { viewModel.onCrossClicked() }
        header?.doOnCloseClick = { viewModel.onChevronClicked() }

        observeFlow(viewModel.events) { handleEvent(it) }
    }

    private fun handleEvent(event: ConfirmStakeEvent) {
        when (event) {
            is ConfirmStakeEvent.CloseFlow -> event.handle()
            ConfirmStakeEvent.NavigateBack -> finish()
        }
    }

    private fun ConfirmStakeEvent.CloseFlow.handle() {
        popBackToRootFragment(
            true,
            StakeFragment::class
        )
        finish()
    }
}