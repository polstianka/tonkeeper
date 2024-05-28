package com.tonapps.tonkeeper.fragment.swap.pager

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tonapps.tonkeeper.fragment.swap.confirm.ConfirmScreen
import com.tonapps.tonkeeper.fragment.swap.currency.CurrencyScreen

class SwapScreenAdapter(
    private val fragment: Fragment
): FragmentStateAdapter(fragment) {

    companion object {

        private var COUNT = 0

        val POSITION_CURRENCY = COUNT++
        val POSITION_CONFIRM = COUNT++
    }

    val currencyScreen: CurrencyScreen?
        get() = findFragmentByPosition(POSITION_CURRENCY) as? CurrencyScreen

    val confirmScreen: ConfirmScreen?
        get() = findFragmentByPosition(POSITION_CONFIRM) as? ConfirmScreen

    override fun getItemCount(): Int {
        return COUNT
    }

    fun findFragmentByPosition(position: Int): Fragment? {
        return fragment.childFragmentManager.findFragmentByTag("f$position")
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            POSITION_CURRENCY -> CurrencyScreen.newInstance()
            POSITION_CONFIRM -> ConfirmScreen.newInstance()
            else -> throw IllegalStateException("Unknown position: $position")
        }
    }
}