package com.tonapps.tonkeeper.ui.screen.stake

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.tonapps.tonkeeper.ui.screen.stake.model.DetailsArgs
import com.tonapps.tonkeeper.ui.screen.stake.model.ExpandedPoolsArgs
import com.tonapps.tonkeeper.ui.screen.stake.options.StakeOptionsMainViewModel
import com.tonapps.tonkeeper.ui.screen.stake.options.StakeOptionsScreensAdapter.Companion.POSITION_DETAILS
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.extensions.collectFlow
import uikit.widget.SimpleRecyclerView

class StakePoolsScreen : Fragment(R.layout.fragment_expanded_pools) {

    private val poolsViewModel: StakePoolsViewModel by viewModel()
    private val optionsMainViewModel: StakeOptionsMainViewModel by activityViewModel()

    private lateinit var pools: SimpleRecyclerView
    private val adapter = PoolsAdapter(
        onClick = {
            val args = DetailsArgs(
                address = it.address,
                name = it.name,
                isApyMax = it.isMaxApy,
                value = it.apyFormatted,
                minDeposit = it.minStake,
                links = it.links
            )
            optionsMainViewModel.setDetailsArgs(args)
            optionsMainViewModel.setCurrentPage(POSITION_DETAILS)
        },
        onCheckedChanged = {
            poolsViewModel.select(it)
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        pools = view.findViewById(R.id.pools)
        pools.adapter = adapter

        collectFlow(optionsMainViewModel.poolsArgs) { args ->
            if (args != null) {
                poolsViewModel.load(args.type, args.maxApyAddress)
            }
        }

        collectFlow(poolsViewModel.items) {
            adapter.submitList(it)
        }
    }

    companion object {
        private const val ARGS_KEY = "args"
        fun newInstance(
            args: ExpandedPoolsArgs
        ): StakePoolsScreen =
            StakePoolsScreen().apply {
                arguments = bundleOf(ARGS_KEY to args)
            }

        fun newInstance() = StakePoolsScreen()
    }
}