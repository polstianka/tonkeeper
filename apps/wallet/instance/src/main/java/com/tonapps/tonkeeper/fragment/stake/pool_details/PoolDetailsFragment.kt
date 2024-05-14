package com.tonapps.tonkeeper.fragment.stake.pool_details

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingService
import com.tonapps.tonkeeperx.R
import core.extensions.observeFlow
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.widget.HeaderView

class PoolDetailsFragment : BaseFragment(R.layout.fragment_pool_details), BaseFragment.BottomSheet {
    companion object {
        fun newInstance(
            service: StakingService,
            pool: StakingPool
        ) = PoolDetailsFragment().apply {
            setArgs(
                PoolDetailsFragmentArgs(service, pool)
            )
        }
    }

    private val viewModel: PoolDetailsViewModel by viewModel()
    private val header: HeaderView?
        get() = view?.findViewById(R.id.fragment_pool_details_header)
    private val apyTextView: TextView?
        get() = view?.findViewById(R.id.fragment_pool_details_apy)
    private val chip: View?
        get() = view?.findViewById(R.id.fragment_pool_details_chip)
    private val minimalDepositTextView: TextView?
        get() = view?.findViewById(R.id.fragment_pool_details_minimal_deposit)

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

        observeFlow(viewModel.events) { handleEvent(it) }
        observeFlow(viewModel.title) { header?.title = it }
        observeFlow(viewModel.apy) { apyTextView?.text = it }
        observeFlow(viewModel.isMaxApyVisible) { chip?.isVisible = it }
        observeFlow(viewModel.minimalDeposit) { minimalDepositTextView?.text = it }
    }

    private fun handleEvent(event: PoolDetailsEvent) {
        when (event) {
            PoolDetailsEvent.FinishFlow -> Log.wtf("###", "finishFlow")
            PoolDetailsEvent.NavigateBack -> finish()
        }
    }
}