package com.tonapps.tonkeeper.fragment.trade.root.presentation

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tonapps.tonkeeper.fragment.trade.domain.model.ExchangeDirection
import com.tonapps.tonkeeper.fragment.trade.exchange.ExchangeFragment
import com.tonapps.tonkeeper.fragment.trade.root.vm.BuySellTabs

internal class BuySellPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = BuySellTabs.entries.size

    override fun createFragment(position: Int): Fragment {
        val tab = BuySellTabs.entries[position]
        return when (tab) {
            BuySellTabs.BUY -> ExchangeFragment.newInstance(ExchangeDirection.BUY)
            BuySellTabs.SELL -> ExchangeFragment.newInstance(ExchangeDirection.SELL)
        }
    }
}