package com.tonapps.tonkeeper.ui.screen.stake

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tonapps.tonkeeper.ui.screen.stake.amount.StakeAmountScreen
import com.tonapps.tonkeeper.ui.screen.stake.confirm.StakeConfirmationScreen
import com.tonapps.tonkeeper.ui.screen.stake.unstake.UnstakeAmountScreen

class StakeScreensAdapter(
    fragment: Fragment,
    private val unstake: Boolean
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            POSITION_AMOUNT -> if (unstake) UnstakeAmountScreen.newInstance() else StakeAmountScreen.newInstance()
            POSITION_CONFIRM -> StakeConfirmationScreen.newInstance()

            else -> throw IllegalStateException("Unknown position: $position")
        }
    }

    companion object {
        const val POSITION_AMOUNT = 0
        const val POSITION_CONFIRM = 1
    }
}