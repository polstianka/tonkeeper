package com.tonapps.tonkeeper.ui.adapter.holder.staking

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.fragment.staking.withdrawal.finish.WithdrawalFinishScreen
import com.tonapps.tonkeeper.helper.flow.CountdownTimer
import com.tonapps.tonkeeper.ui.adapter.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import uikit.extensions.drawable
import uikit.navigation.Navigation.Companion.navigation

class HolderStakingPagePendingAction(parent: ViewGroup) :
    BaseListHolder<Item.StakingPagePendingAction>(parent, R.layout.holder_pool_pending_action) {
    private val titleView = findViewById<AppCompatTextView>(R.id.title)
    private val rateView = findViewById<AppCompatTextView>(R.id.rate)
    private val balanceView = findViewById<AppCompatTextView>(R.id.balance)
    private val balanceFiatView = findViewById<AppCompatTextView>(R.id.balance_currency)

    private val cycleEndFlow = MutableStateFlow<Long>(0)
    private val cycleEndCountdown = CountdownTimer.create(cycleEndFlow)
    private var scope: CoroutineScope? = null

    override fun onBind(item: Item.StakingPagePendingAction) {
        scope?.cancel()
        scope = null

        itemView.background = item.position.drawable(context)

        itemView.isEnabled = item.action == Item.StakingPoolActionType.ReadyWithdraw
        itemView.setOnClickListener{
            context.navigation?.add(WithdrawalFinishScreen.newInstance(item.stake))
        }

        titleView.setText(item.action.title)
        balanceView.text = item.balanceFormat

        if (item.testnet) {
            balanceFiatView.visibility = View.GONE
        } else {
            balanceFiatView.visibility = View.VISIBLE
            balanceFiatView.text = item.fiatFormat
        }

        setRemaining(item, CountdownTimer.remaining(item.cycleEnd))

        if (item.action != Item.StakingPoolActionType.ReadyWithdraw)  {
            cycleEndFlow.value = item.cycleEnd

            val s = CoroutineScope(Dispatchers.Main)
            cycleEndCountdown.onEach{ stamp -> setRemaining(item, stamp)}.launchIn(s)
            scope = s
        }
    }

    private fun setRemaining(item: Item.StakingPagePendingAction, remaining: Long) {
        if (!itemView.isAttachedToWindow || remaining <= 0) {
            scope?.cancel()
            scope = null
        }
        rateView.text = when(item.action) {
            Item.StakingPoolActionType.PendingDeposit -> {
                context.getString(Localization.staking_pending_deposit_simple, CountdownTimer.format(remaining))
            }
            Item.StakingPoolActionType.PendingWithdraw -> {
                context.getString(Localization.staking_pending_withdraw_simple, CountdownTimer.format(remaining))
            }
            Item.StakingPoolActionType.ReadyWithdraw -> getString(Localization.staking_ready_withdraw_simple)
        }
    }

    override fun onUnbind() {
        super.onUnbind()
        scope?.cancel()
        scope = null
    }
}