package com.tonapps.tonkeeper.fragment.staking.deposit.pages.options

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.tonapps.tonkeeper.extensions.findParent
import com.tonapps.tonkeeper.fragment.staking.deposit.DepositScreen
import com.tonapps.tonkeeper.fragment.staking.deposit.DepositScreenViewModel
import com.tonapps.tonkeeper.fragment.staking.deposit.pages.options.impl.StakeImplSelectScreen
import com.tonapps.tonkeeper.fragment.staking.deposit.pages.options.pool.PoolSelectPagerScreen
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import kotlin.math.min

class OptionsPagerScreen : BaseFragment(R.layout.fragment_stake_viewpager) {
    private val poolsViewModel: DepositScreenViewModel by viewModel(ownerProducer = { this.findParent<DepositScreen>() })

    private lateinit var pagerView: ViewPager2
    private lateinit var pageAdapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageAdapter = Adapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pagerView = view.findViewById(R.id.pager)
        pagerView.offscreenPageLimit = 2
        pagerView.isUserInputEnabled = false
        pagerView.adapter = pageAdapter

        collectFlow(poolsViewModel.pageStateFlow) { state ->
            pagerView.setCurrentItem(min(state.selectorPage, 1), state.selectorPrevPage > -1)
        }
    }

    companion object {
        fun newInstance() = OptionsPagerScreen()

        class Adapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

            private companion object {
                private var COUNT = 0
                private val POSITION_IMPLEMENTATION_SELECT = COUNT++
                private val POSITION_POOL_HELPER_SELECT = COUNT++
            }

            override fun getItemCount(): Int {
                return COUNT
            }

            override fun getItemId(position: Int): Long {
                return position.toLong()
            }


            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    POSITION_IMPLEMENTATION_SELECT -> StakeImplSelectScreen.newInstance()
                    POSITION_POOL_HELPER_SELECT -> PoolSelectPagerScreen()
                    else -> throw IllegalStateException("Unknown position: $position")
                }
            }

            override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
                super.onAttachedToRecyclerView(recyclerView)
                recyclerView.isNestedScrollingEnabled = true
            }
        }
    }
}