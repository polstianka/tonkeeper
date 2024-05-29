package com.tonapps.tonkeeper.ui.screen.swap.pager

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tonapps.tonkeeper.ui.screen.swap.amount.SwapAmountScreen
import com.tonapps.tonkeeper.ui.screen.swap.choose.SwapChooseScreen
import com.tonapps.tonkeeper.ui.screen.swap.confirm.SwapConfirmScreen
import com.tonapps.tonkeeper.ui.screen.swap.settings.SwapSettingsScreen


class SwapScreenAdapter(
    private val fragment: Fragment
) : FragmentStateAdapter(fragment) {

    companion object {
        private var COUNT = 0
        val POSITION_AMOUNT = COUNT++
        val POSITION_CHOOSE = COUNT++
        val POSITION_SETTINGS = COUNT++
        val POSITION_CONFIRM = COUNT++
    }

    val swapAmountScreen: SwapAmountScreen?
        get() = findFragmentByPosition(POSITION_AMOUNT) as? SwapAmountScreen

    val swapChooseScreen: SwapChooseScreen?
        get() = findFragmentByPosition(POSITION_CHOOSE) as? SwapChooseScreen

    //val swapConfirmScreen: SwapConfirmScreen?
    //    get() = findFragmentByPosition(POSITION_CONFIRM) as? SwapConfirmScreen

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
            POSITION_AMOUNT -> SwapAmountScreen.newInstance()
            POSITION_SETTINGS -> SwapSettingsScreen.newInstance()
            POSITION_CHOOSE -> SwapChooseScreen.newInstance()
            POSITION_CONFIRM -> SwapConfirmScreen.newInstance()
            else -> throw IllegalStateException("Unknown position: $position")
        }
    }
}