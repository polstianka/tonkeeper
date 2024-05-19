package com.tonapps.tonkeeper.ui.screen.stake.options

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tonapps.tonkeeper.ui.screen.stake.PoolDetailsScreen
import com.tonapps.tonkeeper.ui.screen.stake.StakePoolsScreen

class StakeOptionsScreensAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            POSITION_OPTIONS -> StakeOptionsScreen.newInstance()
            POSITION_POOLS -> StakePoolsScreen.newInstance()
            POSITION_DETAILS -> PoolDetailsScreen.newInstance()
            else -> throw IllegalStateException("Unknown position: $position")
        }
    }

    companion object {
        const val POSITION_OPTIONS = 0
        const val POSITION_POOLS = 1
        const val POSITION_DETAILS = 2
    }
}