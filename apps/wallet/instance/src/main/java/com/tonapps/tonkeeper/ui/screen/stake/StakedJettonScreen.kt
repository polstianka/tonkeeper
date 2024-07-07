package com.tonapps.tonkeeper.ui.screen.stake

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.ConcatAdapter
import com.tonapps.tonkeeper.core.history.list.HistoryAdapter
import com.tonapps.tonkeeper.fragment.chart.list.ChartAdapter
import com.tonapps.tonkeeper.fragment.jetton.list.JettonAdapter
import com.tonapps.tonkeeper.fragment.jetton.list.JettonItemDecoration
import com.tonapps.tonkeeper.fragment.jetton.list.JettonItemVerticalOffset
import com.tonapps.tonkeeper.ui.screen.stake.StakedJettonViewModel.Companion.HISTORY_TAB_ID
import com.tonapps.tonkeeperx.R
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.collectFlow
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.toggleVisibilityAnimation
import uikit.extensions.topScrolled
import uikit.mvi.AsyncState
import uikit.widget.HeaderView
import uikit.widget.SimpleRecyclerView

class StakedJettonScreen : BaseFragment(R.layout.fragment_staked_jetton), BaseFragment.SwipeBack {

    private val stakedJettonViewModel: StakedJettonViewModel by viewModel()

    private lateinit var headerView: HeaderView
    private lateinit var shimmerView: View
    private lateinit var listView: SimpleRecyclerView

    private val chartAdapter = ChartAdapter {}
    private val jettonAdapter = JettonAdapter { stakedJettonViewModel.onTabClicked(it) }
    private val historyAdapter = HistoryAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val args = arguments?.getParcelable<StakedJettonArgs>(ARGS_KEY) ?: error("Provide args")
        headerView = view.findViewById(R.id.header)
        headerView.title = args.title
        headerView.doOnCloseClick = { finish() }

        shimmerView = view.findViewById(R.id.shimmer)

        stakedJettonViewModel.load(args.address)

        val adapter = ConcatAdapter(chartAdapter, jettonAdapter, historyAdapter)
        listView = view.findViewById(R.id.list)
        listView.adapter = adapter
        listView.addItemDecoration(JettonItemDecoration(view.context))
        listView.addItemDecoration(JettonItemVerticalOffset(view.context))
        listView.addItemDecoration(ChartWithOutHistoryItemDecoration(view.context))
        listView.itemAnimator = ExitEnterAnimator()
        listView.applyNavBottomPadding(requireContext().getDimensionPixelSize(uikit.R.dimen.offsetMedium))
        collectFlow(listView.topScrolled, headerView::setDivider)
        collectFlow(stakedJettonViewModel.uiState) { state ->
            if (state.asyncState == AsyncState.Default) {
                chartAdapter.submitList(state.chartItems)
                jettonAdapter.submitList(state.items)
                historyAdapter.submitList(if (state.selectedTab == HISTORY_TAB_ID) state.historyItems else emptyList()) {
                    toggleVisibilityAnimation(shimmerView, listView)
                }
            }
        }
    }

    companion object {
        private const val ARGS_KEY = "args"
        fun newInstance(args: StakedJettonArgs) = StakedJettonScreen().apply {
            arguments = bundleOf(ARGS_KEY to args)
        }
    }
}

@Parcelize
data class StakedJettonArgs(
    val address: String,
    val title: String,
) : Parcelable