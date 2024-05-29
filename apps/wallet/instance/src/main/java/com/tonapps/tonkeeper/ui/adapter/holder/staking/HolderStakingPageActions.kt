package com.tonapps.tonkeeper.ui.adapter.holder.staking

import android.view.View
import android.view.ViewGroup
import com.tonapps.tonkeeper.fragment.staking.deposit.DepositScreen
import com.tonapps.tonkeeper.fragment.staking.withdrawal.WithdrawalScreen
import com.tonapps.tonkeeper.ui.adapter.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.wallet.data.account.WalletType
import uikit.navigation.Navigation
import java.math.BigInteger

class HolderStakingPageActions(
    parent: ViewGroup
) : BaseListHolder<Item.StakingPageActions>(parent, R.layout.holder_staking_page_actions) {

    private val navigation = Navigation.from(context)
    private val stakeView = findViewById<View>(R.id.stake)
    private val unstakeView = findViewById<View>(R.id.unstake)

    override fun onBind(item: Item.StakingPageActions) {
        stakeView.isEnabled = item.walletType != WalletType.Watch
        stakeView.setOnClickListener {
            navigation?.add(DepositScreen.newInstance(item.poolAddress))
        }

        unstakeView.isEnabled = stakeView.isEnabled && (item.stake?.balance?.stake?.amountNano?.let { it > BigInteger.ZERO } ?: false)
        unstakeView.setOnClickListener {
            navigation?.add(WithdrawalScreen.newInstance(item.stake!!))
        }
    }
}