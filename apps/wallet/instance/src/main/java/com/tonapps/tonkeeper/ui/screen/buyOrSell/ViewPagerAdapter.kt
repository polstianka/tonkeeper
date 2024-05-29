package com.tonapps.tonkeeper.ui.screen.buyOrSell

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.DealState
import uikit.base.BaseFragment

class ViewPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val nextNavHandler: (selectedTon: Double) -> Unit
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> BasicBuyOrSellScreen(DealState.BUY, nextNavHandler = nextNavHandler)
            1 -> BasicBuyOrSellScreen(DealState.SELL, nextNavHandler = nextNavHandler)
            else -> BasicBuyOrSellScreen(DealState.BUY, nextNavHandler = nextNavHandler)
        }
    }
}
