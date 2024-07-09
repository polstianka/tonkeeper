package com.tonapps.tonkeeper.fragment.stake.pick_pool

import android.os.Bundle
import android.util.Log
import android.view.View
import com.tonapps.tonkeeper.extensions.popBackToRootFragment
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingService
import com.tonapps.tonkeeper.fragment.stake.pick_option.rv.StakingOptionAdapter
import com.tonapps.tonkeeper.fragment.stake.pick_pool.rv.PickPoolAdapter
import com.tonapps.tonkeeper.fragment.stake.pool_details.PoolDetailsFragment
import com.tonapps.tonkeeper.fragment.stake.root.StakeFragment
import com.tonapps.uikit.icon.UIKitIcon
import com.tonapps.wallet.data.core.WalletCurrency
import core.extensions.observeFlow
import uikit.base.BaseFragment
import uikit.base.BaseListFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.navigation.Navigation.Companion.navigation

class PickPoolFragment : BaseListFragment(), BaseFragment.BottomSheet {

    companion object {
        fun newInstance(
            service: StakingService,
            picked: StakingPool,
            currency: WalletCurrency
        ) = PickPoolFragment().apply {
            setArgs(
                PickPoolFragmentArgs(service, picked, currency)
            )
        }
    }

    private val viewModel: PickPoolViewModel by viewModel()
    private val adapter = PickPoolAdapter { viewModel.onItemClicked(it) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.provideArguments(PickPoolFragmentArgs(requireArguments()))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerView.setIcon(UIKitIcon.ic_chevron_left_16)
        headerView.doOnCloseClick = { viewModel.onChevronClicked() }
        headerView.doOnActionClick = { viewModel.onCloseClicked() }

        setAdapter(adapter)

        observeFlow(viewModel.title) { setTitle(it) }
        observeFlow(viewModel.events) { handleEvent(it) }
        observeFlow(viewModel.items) { adapter.submitList(it) }
    }

    private fun handleEvent(event: PickPoolEvents) {
        when (event) {
            is PickPoolEvents.CloseFlow -> event.handle()
            PickPoolEvents.NavigateBack -> finish()
            is PickPoolEvents.NavigateToPoolDetails -> event.handle()
        }
    }

    private fun PickPoolEvents.CloseFlow.handle() {
        popBackToRootFragment(includingRoot = true, StakeFragment::class)
        finish()
    }

    private fun PickPoolEvents.NavigateToPoolDetails.handle() {
        val fragment = PoolDetailsFragment.newInstance(service, pool, currency)
        navigation?.add(fragment)
    }
}