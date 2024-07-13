package com.tonapps.tonkeeper.ui.screen.stake.options

import android.os.Bundle
import android.view.View
import androidx.core.view.postDelayed
import com.tonapps.tonkeeper.ui.screen.stake.pager.PagerScreen
import com.tonapps.tonkeeper.ui.screen.stake.pager.StakeScreenAdapter
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.getDimensionPixelSize
import uikit.widget.SimpleRecyclerView

class StakeOptionsScreen : PagerScreen<StakeOptionsScreenState, StakeOptionsScreenEffect, StakeOptionsScreenFeature>(R.layout.fragment_stake_options) {

    companion object {
        fun newInstance() = StakeOptionsScreen()
    }

    override val feature: StakeOptionsScreenFeature by viewModel()

    private lateinit var recyclerView: SimpleRecyclerView
    private val adapter = StateOptionsAdapter { poolImplementationType, poolInfo ->
        when {
            poolInfo != null -> {
                stakeFeature.setPoolCandidate(poolInfo)
                stakeFeature.setCurrentPage(StakeScreenAdapter.POSITION_CHOOSE)
            }
            poolImplementationType != null -> {
                feature.implChosen(poolImplementationType)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.list)
        recyclerView.adapter = adapter
        recyclerView.applyNavBottomPadding(requireContext().getDimensionPixelSize(uikit.R.dimen.offsetMedium))
    }

    override fun newUiState(state: StakeOptionsScreenState) {
        adapter.submitList(state.optionItems) {
            recyclerView.scrollToPosition(0)
        }
        if (state.poolImplementation != null) {
            stakeFeature.setHeaderTitle(state.poolImplementation.name)
        } else {
            if (isVisibleForUser()) {
                stakeFeature.setHeaderTitle(getString(Localization.options))
            }
        }
    }

    fun onBack(): Boolean {
        if (feature.uiState.value.poolImplementation == null) {
            return true
        }
        feature.toLiquidState()
        return false
    }

    override fun onVisibleChange(visible: Boolean) {
        super.onVisibleChange(visible)
        if (visible) {
            stakeFeature.data.value?.let {
                feature.update(it)
            }
            stakeFeature.setHeaderTitle(getString(Localization.options))
            stakeFeature.setHeaderVisible(true)
        }
    }
}