package com.tonapps.tonkeeper.fragment.stake.pool_details

import android.os.Bundle
import android.view.View
import android.widget.Button
import com.tonapps.tonkeeper.extensions.popBackToRootFragment
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingService
import com.tonapps.tonkeeper.fragment.stake.root.StakeFragment
import com.tonapps.tonkeeper.fragment.stake.ui.LiquidStakingDetailsView
import com.tonapps.tonkeeper.fragment.stake.ui.PoolDetailsView
import com.tonapps.tonkeeper.fragment.stake.ui.PoolLinksView
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.data.core.WalletCurrency
import core.extensions.observeFlow
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.dp
import uikit.extensions.setThrottleClickListener
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView

class PoolDetailsFragment : BaseFragment(R.layout.fragment_pool_details), BaseFragment.BottomSheet {
    companion object {
        const val REQUEST_KEY_PICK_POOL = "REQUEST_KEY_PICK_POOL "
        fun newInstance(
            service: StakingService,
            pool: StakingPool,
            currency: WalletCurrency
        ) = PoolDetailsFragment().apply {
            setArgs(
                PoolDetailsFragmentArgs(service, pool, currency)
            )
        }
    }

    private val viewModel: PoolDetailsViewModel by viewModel()
    private val header: HeaderView?
        get() = view?.findViewById(R.id.fragment_pool_details_header)
    private val links: PoolLinksView?
        get() = view?.findViewById(R.id.fragment_pool_details_links)
    private val button: Button?
        get() = view?.findViewById(R.id.fragment_pool_details_button)
    private val footer: View?
        get() = view?.findViewById(R.id.fragment_pool_details_footer)
    private val liquidStakingDetailsView: LiquidStakingDetailsView?
        get() = view?.findViewById(R.id.fragment_pool_details_liquid_staking_details)
    private val poolDetails: PoolDetailsView?
        get() = view?.findViewById(R.id.fragment_pool_details_pool_details)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.provideArgs(PoolDetailsFragmentArgs(requireArguments()))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        header?.doOnActionClick = { viewModel.onCloseClicked() }
        header?.doOnCloseClick = { viewModel.onChevronClicked() }

        button?.setThrottleClickListener { viewModel.onButtonClicked() }

        footer?.applyNavBottomPadding(16f.dp)

        links?.setOnChipClicked { viewModel.onChipClicked(it) }

        observeFlow(viewModel.events) { handleEvent(it) }
        observeFlow(viewModel.title) { header?.title = it }
        observeFlow(viewModel.pool) { poolDetails?.setPool(it) }
        observeFlow(viewModel.chips) { links?.applyChips(it) }
        observeFlow(viewModel.liquidJetton) { liquidStakingDetailsView?.applyLiquidJetton(it) }
    }

    private fun handleEvent(event: PoolDetailsEvent) {
        when (event) {
            PoolDetailsEvent.FinishFlow -> popBackToRoot(includingRoot = true)
            PoolDetailsEvent.NavigateBack -> finish()
            is PoolDetailsEvent.NavigateToLink -> navigation?.openURL(event.url, external = true)
            is PoolDetailsEvent.PickPool -> event.handle()
        }
    }

    private fun PoolDetailsEvent.PickPool.handle() {
        val result = PoolDetailsFragmentResult(pool)
        navigation?.setFragmentResult(REQUEST_KEY_PICK_POOL, result.toBundle())
        popBackToRoot()
    }

    private fun popBackToRoot(includingRoot: Boolean = false) {
        popBackToRootFragment(includingRoot = includingRoot, StakeFragment::class)
        finish()
    }
}