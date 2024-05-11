package com.tonapps.tonkeeper.ui.screen.stake

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.ActionCellRadioView
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
            state.info.forEach { info ->
                when (info) {
                    is StakeOptionsUiState.StakeInfo.Liquid -> {
                        liquidStakingLayout.addView(ActionCellRadioView(requireContext()).apply {
                            title = info.name
                            subtitle = info.description
                            titleBadgeText = if (info.isMaxApy) "max apy" else null
                            position = info.position
                            checked = info.selected
                            setOnClickListener {
                                val args = DetailsArgs(
                                    name = info.name,
                                    isApyMax = info.isMaxApy,
                                    value = info.maxApyFormatted,
                                    minDeposit = 0f,
                                    currency = "TON",
                                    links = info.links
                                )
                                navigation?.add(PoolDetailsScreen.newInstance(args))
                            }
                        })
                    }

                    is StakeOptionsUiState.StakeInfo.Other -> {
                        otherLayout.addView(ActionCellView(requireContext()).apply {
                            title = info.name
                            subtitle = info.description
                            titleBadgeText = if (info.isMaxApy) "max apy" else null
                            position = info.position
                        })
                    }
                }
            }
        }
    }
}