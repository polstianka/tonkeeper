package com.tonapps.tonkeeper.fragment.trade.root.presentation

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tonapps.tonkeeper.fragment.trade.buy.BuyFragment
import com.tonapps.tonkeeper.fragment.trade.root.vm.BuySellTabs
import com.tonapps.tonkeeper.fragment.trade.sell.SellFragment

internal class BuySellPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = BuySellTabs.entries.size

    override fun createFragment(position: Int): Fragment {
        val tab = BuySellTabs.entries[position]
        return when (tab) {
            BuySellTabs.BUY -> BuyFragment.newInstance()
            BuySellTabs.SELL -> SellFragment.newInstance()
        }
    }
}