package com.tonapps.tonkeeper.ui.screen.stake.options

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.tonapps.tonkeeper.ui.screen.stake.model.DetailsArgs
import com.tonapps.tonkeeper.ui.screen.stake.model.ExpandedPoolsArgs
import com.tonapps.tonkeeper.ui.screen.stake.options.StakeOptionsScreensAdapter.Companion.POSITION_DETAILS
import com.tonapps.tonkeeper.ui.screen.stake.options.StakeOptionsScreensAdapter.Companion.POSITION_POOLS
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.widget.ActionCellRadioView
import uikit.widget.ActionCellView

class StakeOptionsScreen : BaseFragment(R.layout.fragment_stake_options) {

    private val optionsViewModel: StakeOptionsViewModel by viewModel()
    private val optionsMainViewModel: StakeOptionsMainViewModel by activityViewModel()

    private lateinit var liquidStakingLayout: ViewGroup
    private lateinit var otherLayout: ViewGroup

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
            iconRes = info.iconRes
            iconTint = 0
            isRoundedIcon = true

            setOnClickListener {
                optionsMainViewModel.setPoolsArgs(
                    ExpandedPoolsArgs(
                        type = info.type,
                        maxApyAddress = info.maxApyAddress,
                        name = info.name
                    )
                )
                optionsMainViewModel.setCurrentPage(POSITION_POOLS)
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
            iconRes = info.iconRes
            iconTint = 0
            isRoundedIcon = true
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
                optionsMainViewModel.setDetailsArgs(args)
                optionsMainViewModel.setCurrentPage(POSITION_DETAILS)
            }
        })
    }

    private fun getDescription(info: StakeOptionsUiState.StakeInfo) =
        info.description + "\n" + getString(
            Localization.apy_percent_placeholder,
            info.maxApyFormatted
        )

    companion object {
        fun newInstance() = StakeOptionsScreen()
    }
}