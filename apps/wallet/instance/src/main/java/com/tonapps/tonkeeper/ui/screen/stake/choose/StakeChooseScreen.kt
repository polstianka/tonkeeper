package com.tonapps.tonkeeper.ui.screen.stake.choose

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import com.tonapps.tonkeeper.ui.screen.stake.StakingRepository
import com.tonapps.tonkeeper.ui.screen.stake.pager.PagerScreen
import com.tonapps.tonkeeper.ui.screen.stake.pager.StakeScreenAdapter
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.extensions.dp
import uikit.navigation.Navigation.Companion.navigation

class StakeChooseScreen : PagerScreen<StakeChooseScreenState, StakeChooseScreenEffect, StakeChooseScreenFeature>(R.layout.fragment_stake_choose) {

    companion object {
        fun newInstance() = StakeChooseScreen()
    }

    override val feature: StakeChooseScreenFeature by viewModel()

    private lateinit var continueButton: Button
    private lateinit var minDeposit: AppCompatTextView
    private lateinit var minApy: AppCompatTextView
    private lateinit var links: FlowLayout
    private lateinit var linksHeader: View
    private lateinit var maxapy: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        minDeposit = view.findViewById(R.id.minimal_deposit_value)
        minApy = view.findViewById(R.id.apy_value)
        links = view.findViewById(R.id.links)
        linksHeader = view.findViewById(R.id.links_hint)
        maxapy = view.findViewById(R.id.maxapy)
        continueButton = view.findViewById(R.id.continue_action)
        continueButton.setOnClickListener { next() }
    }

    private fun next() {
        stakeFeature.setPool(feature.uiState.value.selectedPool)
        stakeFeature.setCurrentPage(StakeScreenAdapter.POSITION_AMOUNT)
    }

    @SuppressLint("SetTextI18n")
    override fun newUiState(state: StakeChooseScreenState) {
        minDeposit.text = state.minDeposit
        minApy.text = state.apy
        linksHeader.isVisible = state.socials.isNotEmpty()
        links.removeAllViews()
        maxapy.isVisible = StakingRepository.maxApy == state.selectedPool?.apy
        state.socials.forEachIndexed { index, s ->
            val view = SocialsView(
                requireContext()
            ).apply {
                setData(SocialsView.Social.fromString(s))
            }
            view.setOnClickListener {
                navigation?.openURL(s, true)
            }
            view.layoutParams = FlowLayout.LayoutParams(8.dp, 8.dp)
            links.addView(view)
        }
    }

    override fun onVisibleChange(visible: Boolean) {
        super.onVisibleChange(visible)
        if (visible) {
            stakeFeature.data.value?.poolInfoCandidate?.let {
                feature.update(it)
                stakeFeature.setHeaderTitle(it.name)
                stakeFeature.setHeaderVisible(true)
            }
        }
    }
}