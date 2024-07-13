package com.tonapps.tonkeeper.ui.screen.buysell.pager

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tonapps.tonkeeper.ui.screen.buysell.amount.BuySellAmountScreen
import com.tonapps.tonkeeper.ui.screen.buysell.confirm.BuySellConfirmScreen
import com.tonapps.tonkeeper.ui.screen.buysell.currency.BuySellCurrencyScreen
import com.tonapps.tonkeeper.ui.screen.buysell.operator.BuySellOperatorScreen

class BuySellScreenAdapter(
    private val fragment: Fragment
) : FragmentStateAdapter(fragment) {

    companion object {

        private var COUNT = 0

        val POSITION_AMOUNT = COUNT++
        val POSITION_OPERATOR = COUNT++
        val POSITION_CURRENCY = COUNT++
        val POSITION_CONFIRM = COUNT++
    }

    val amountScreen: BuySellAmountScreen?
        get() = findFragmentByPosition(POSITION_AMOUNT) as? BuySellAmountScreen

    val confirmScreen: BuySellConfirmScreen?
        get() = findFragmentByPosition(POSITION_CONFIRM) as? BuySellConfirmScreen

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
            POSITION_AMOUNT -> BuySellAmountScreen.newInstance()
            POSITION_OPERATOR -> BuySellOperatorScreen.newInstance()
            POSITION_CURRENCY -> BuySellCurrencyScreen.newInstance()
            POSITION_CONFIRM -> BuySellConfirmScreen.newInstance()
            else -> throw IllegalStateException("Unknown position: $position")
        }
    }
}