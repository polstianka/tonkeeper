package com.tonapps.tonkeeper.fragment.staking

import android.os.Bundle
import android.view.View
import com.tonapps.tonkeeper.fragment.staking.withdrawal.finish.WithdrawalFinishScreen
import com.tonapps.tonkeeper.ui.adapter.Adapter
import com.tonapps.tonkeeper.ui.adapter.ItemDecoration
import com.tonapps.tonkeeper.ui.adapter.ItemHorizontalDecoration
import com.tonapps.tonkeeper.ui.component.BlurredRecyclerView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.backgroundTransparentColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.extensions.dp
import uikit.extensions.toggleVisibilityAnimation
import uikit.extensions.topScrolled
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.FooterViewEmpty
import uikit.widget.HeaderView

class StakingScreen : BaseFragment(R.layout.fragment_account_stake), BaseFragment.SwipeBack {
    private val stakingViewModel: StakingScreenViewModel by viewModel()

    companion object {
        private const val POOL_ADDRESS_KEY = "POOL_ADDRESS_KEY"
        private const val POOL_NAME_KEY = "POOL_NAME_KEY"

        fun newInstance(poolAddress: String, poolName: String): BaseFragment {
            val screen = StakingScreen()
            screen.arguments = Bundle().apply {
                putString(POOL_ADDRESS_KEY, poolAddress)
                putString(POOL_NAME_KEY, poolName)
            }
            return screen
        }
    }

    private val stakingAdapter = Adapter()

    private val poolAddress: String by lazy {
        arguments?.getString(POOL_ADDRESS_KEY)!!
    }

    private val poolName: String by lazy {
        arguments?.getString(POOL_NAME_KEY) ?: ""
    }

    private lateinit var headerView: HeaderView
    private lateinit var footerView: FooterViewEmpty
    private lateinit var shimmerView: View
    private lateinit var listView: BlurredRecyclerView
    private var needToggle = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerView = view.findViewById(R.id.header)
        headerView.title = poolName
        headerView.doOnCloseClick = { finish() }
        headerView.setColor(requireContext().backgroundTransparentColor)

        footerView = view.findViewById(R.id.footer)
        footerView.setColor(requireContext().backgroundTransparentColor)

        shimmerView = view.findViewById(R.id.shimmer)

        listView = view.findViewById(R.id.list)
        listView.addItemDecoration(ItemDecoration)
        listView.addItemDecoration(ItemHorizontalDecoration(16.dp))
        listView.blurredPaddingTop = 64.dp
        listView.adapter = stakingAdapter



        collectFlow(listView.topScrolled, headerView::setDivider)

        collectFlow(stakingViewModel.uiItemsFlow) { items ->
            stakingAdapter.submitList(items)
            if (needToggle) {
                toggleVisibilityAnimation(shimmerView, listView)
                needToggle = false
            }
        }
    }

    override fun onEndShowingAnimation() {
        super.onEndShowingAnimation()
        stakingViewModel.load({ stake ->
            context?.navigation?.add(WithdrawalFinishScreen(stake))
        }, poolAddress = poolAddress)
    }
}