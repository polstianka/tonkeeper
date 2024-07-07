package com.tonapps.tonkeeper.fragment.chart.list.holder

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.tonapps.tonkeeper.fragment.chart.list.ChartItem
import com.tonapps.tonkeeper.ui.screen.stake.StakeMainScreen
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.data.account.WalletType
import uikit.navigation.Navigation

class ChartActionsStakedHolder(
    parent: ViewGroup
) : ChartHolder<ChartItem.ActionsStaked>(parent, R.layout.view_jetton_actions_staked) {

    private val navigation = Navigation.from(context)
    private val stakeView = findViewById<View>(R.id.stake)
    private val unstakeView = findViewById<View>(R.id.unstake)

    override fun onBind(item: ChartItem.ActionsStaked) {
        stakeView.setOnClickListener {
            navigation?.add(StakeMainScreen.newInstance(item.poolAddress))
        }

        stakeView.isVisible = item.walletType != WalletType.Watch

        unstakeView.setOnClickListener {
            navigation?.add(StakeMainScreen.newInstance(item.poolAddress, true))
        }
    }
}