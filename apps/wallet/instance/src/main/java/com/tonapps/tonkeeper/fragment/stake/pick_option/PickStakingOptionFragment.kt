package com.tonapps.tonkeeper.fragment.stake.pick_option

import android.os.Bundle
import android.util.Log
import android.view.View
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingService
import com.tonapps.tonkeeperx.R
import core.extensions.observeFlow
import uikit.base.BaseFragment
import uikit.widget.HeaderView
import org.koin.androidx.viewmodel.ext.android.viewModel

class PickStakingOptionFragment : BaseFragment(
    R.layout.fragment_pick_staking_option
), BaseFragment.BottomSheet {

    companion object {
        fun newInstance(
            options: List<StakingService>,
            picked: StakingPool
        ) = PickStakingOptionFragment().apply {
            setArgs(
                PickStakingOptionFragmentArgs(options, picked)
            )
        }
    }

    private val viewModel: PickStakingOptionViewModel by viewModel()
    private val header: HeaderView?
        get() = view?.findViewById(R.id.fragment_pick_staking_option_header)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        header?.doOnCloseClick = { viewModel.onChevronClicked() }
        header?.doOnActionClick = { viewModel.onCrossClicked() }

        observeFlow(viewModel.events) { handleEvent(it) }
    }

    private fun handleEvent(event: PickStakingOptionEvent) {
        when (event) {
            PickStakingOptionEvent.CloseFlow -> Log.wtf("###", "closeFlow")
            PickStakingOptionEvent.NavigateBack -> finish()
        }

    }
}