package com.tonapps.tonkeeper.ui.screen.stake

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.tonapps.tonkeeper.ui.screen.stake.model.DetailsArgs
import com.tonapps.tonkeeper.ui.screen.stake.model.ExpandedPoolsArgs
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.localization.Localization
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
            liquidStakingLayout.removeAllViews()
            otherLayout.removeAllViews()
            state.info.forEach { info ->
                when (info) {
                    is StakeOptionsUiState.StakeInfo.Liquid -> {
                        addToLiquidStaking(info)
                    }

                    is StakeOptionsUiState.StakeInfo.Other -> {
                        addToOther(info)
                    }
                }
            }
        }
    }

    private fun addToOther(info: StakeOptionsUiState.StakeInfo.Other) {
        otherLayout.addView(ActionCellView(requireContext()).apply {
            title = info.name
            subtitle = getDescription(info)
            titleBadgeText = getString(Localization.max_apy).takeIf { info.isMaxApy }
            position = info.position

            setOnClickListener {
                navigation?.add(
                    StakePoolsFragment.newInstance(
                        ExpandedPoolsArgs(
                            type = info.type,
                            maxApyAddress = info.maxApyAddress,
                            name = info.name
                        )
                    )
                )
            }
        })
    }

    private fun addToLiquidStaking(info: StakeOptionsUiState.StakeInfo.Liquid) {
        liquidStakingLayout.addView(ActionCellRadioView(requireContext()).apply {
            title = info.name
            subtitle = getDescription(info)
            titleBadgeText = getString(Localization.max_apy).takeIf { info.isMaxApy }
            position = info.position
            checked = info.selected
            onCheckedChange = { optionsViewModel.select(info.address) }
            setOnClickListener {
                val args = DetailsArgs(
                    address = info.address,
                    name = info.name,
                    isApyMax = info.isMaxApy,
                    value = info.maxApyFormatted,
                    minDeposit = info.minStake,
                    links = info.links
                )
                navigation?.add(PoolDetailsScreen.newInstance(args))
            }
        })
    }

    private fun getDescription(info: StakeOptionsUiState.StakeInfo) =
        info.description + "\n" + getString(
            Localization.apy_percent_placeholder,
            info.maxApyFormatted
        )
}