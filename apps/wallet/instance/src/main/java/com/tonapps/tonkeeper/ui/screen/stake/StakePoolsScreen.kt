package com.tonapps.tonkeeper.ui.screen.stake

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.tonapps.tonkeeper.ui.screen.stake.model.DetailsArgs
import com.tonapps.tonkeeper.ui.screen.stake.model.ExpandedPoolsArgs
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView
import uikit.widget.SimpleRecyclerView

class StakePoolsFragment :
    BaseFragment(R.layout.fragment_expanded_pools),
    BaseFragment.BottomSheet {

    private val poolsViewModel: StakePoolsViewModel by viewModel()

    private lateinit var headerView: HeaderView
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
            navigation?.add(PoolDetailsScreen.newInstance(args))
        },
        onCheckedChanged = {
            poolsViewModel.select(it)
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        headerView = view.findViewById(R.id.header)
        headerView.doOnActionClick = { finish() }

        pools = view.findViewById(R.id.pools)
        pools.adapter = adapter

        val args = arguments?.getParcelable<ExpandedPoolsArgs>(ARGS_KEY) ?: error("Provide args")
        headerView.title = args.name
        poolsViewModel.load(args.type, args.maxApyAddress)

        collectFlow(poolsViewModel.items) {
            adapter.submitList(it)
        }
    }

    companion object {
        private const val ARGS_KEY = "args"
        fun newInstance(
            args: ExpandedPoolsArgs
        ): StakePoolsFragment =
            StakePoolsFragment().apply {
                arguments = bundleOf(ARGS_KEY to args)
            }
    }
}