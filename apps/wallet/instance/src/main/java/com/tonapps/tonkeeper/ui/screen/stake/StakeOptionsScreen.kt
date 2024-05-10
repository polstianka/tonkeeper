package com.tonapps.tonkeeper.ui.screen.stake

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.widget.ActionCellView
import uikit.widget.HeaderView

class StakeOptionsScreen : BaseFragment(R.layout.fragment_stake_options), BaseFragment.BottomSheet {

    private val optionsViewModel: StakeOptionsViewModel by viewModel()

    private lateinit var headerView: HeaderView
    private lateinit var liquidStakingLayout: ViewGroup
    private lateinit var otherLayout: ViewGroup

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        headerView = view.findViewById(R.id.header)
        headerView.doOnActionClick = { finish() }
        liquidStakingLayout = view.findViewById(R.id.liquid_staking_items)
        otherLayout = view.findViewById(R.id.other_items)

        collectFlow(optionsViewModel.uiState) { state ->
            state.info.forEach {
                when (it) {
                    is StakeOptionsUiState.StakeInfo.Liquid -> {
                        liquidStakingLayout.addView(ActionCellView(requireContext()).apply {
                            title = it.name
                            subtitle = it.description
                            titleBadgeText = if (it.isMaxApy) "max apy" else null
                            position = it.position
                        })
                    }

                    is StakeOptionsUiState.StakeInfo.Other -> {
                        otherLayout.addView(ActionCellView(requireContext()).apply {
                            title = it.name
                            subtitle = it.description
                            titleBadgeText = if (it.isMaxApy) "max apy" else null
                            position = it.position
                        })
                    }
                }
            }
        }
    }
}