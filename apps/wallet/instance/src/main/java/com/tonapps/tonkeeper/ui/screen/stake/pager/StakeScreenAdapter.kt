package com.tonapps.tonkeeper.ui.screen.stake.pager


import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tonapps.tonkeeper.ui.screen.stake.amount.StakeAmountScreen
import com.tonapps.tonkeeper.ui.screen.stake.choose.StakeChooseScreen
import com.tonapps.tonkeeper.ui.screen.stake.confirm.StakeConfirmScreen
import com.tonapps.tonkeeper.ui.screen.stake.options.StakeOptionsScreen

class StakeScreenAdapter(
    private val fragment: Fragment
) : FragmentStateAdapter(fragment) {

    companion object {

        private var COUNT = 0

        val POSITION_AMOUNT = COUNT++

        val POSITION_CONFIRM = COUNT++

        val POSITION_OPTIONS = COUNT++

        val POSITION_CHOOSE = COUNT++
    }

    val stakeAmountScreen: StakeAmountScreen?
        get() = findFragmentByPosition(POSITION_AMOUNT) as? StakeAmountScreen

    val stakeOptionsScreen: StakeOptionsScreen?
        get() = findFragmentByPosition(POSITION_OPTIONS) as? StakeOptionsScreen

    val stakeConfirmScreen: StakeConfirmScreen?
        get() = findFragmentByPosition(POSITION_CONFIRM) as? StakeConfirmScreen

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
            POSITION_AMOUNT -> StakeAmountScreen.newInstance()
            POSITION_OPTIONS -> StakeOptionsScreen.newInstance()
            POSITION_CONFIRM -> StakeConfirmScreen.newInstance()
            POSITION_CHOOSE -> StakeChooseScreen.newInstance()
            else -> throw IllegalStateException("Unknown position: $position")
        }
    }
}