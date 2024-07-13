package com.tonapps.tonkeeper.ui.screen.token.list.holder

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.tonapps.tonkeeper.fragment.send.SendScreen
import com.tonapps.tonkeeper.ui.screen.qr.QRScreen
import com.tonapps.tonkeeper.ui.screen.stake.StakeScreen
import com.tonapps.tonkeeper.ui.screen.swap.SwapScreen
import com.tonapps.tonkeeper.ui.screen.swap.SwapScreenNew
import com.tonapps.tonkeeper.ui.screen.token.list.Item
import com.tonapps.tonkeeperx.R
import uikit.navigation.Navigation

class ActionsHolder(parent: ViewGroup): Holder<Item.Actions>(parent, R.layout.view_token_actions) {

    private val navigation: Navigation?
        get() = Navigation.from(context)

    private val sendView = findViewById<View>(R.id.send)
    private val receiveView = findViewById<View>(R.id.receive)
    private val swapView = findViewById<View>(R.id.swap)
    private val stakeView = findViewById<View>(R.id.stake)
    private val unstakeView = findViewById<View>(R.id.unstake)

    override fun onBind(item: Item.Actions) {

        if (item.token.isStaking) {
            sendView.isVisible = false
            receiveView.isVisible = false
            swapView.isVisible = false
            stakeView.isVisible = true
            unstakeView.isVisible = true
        } else {
            sendView.isVisible = true
            receiveView.isVisible = true
            swapView.isVisible = true
            stakeView.isVisible = false
            unstakeView.isVisible = false
        }

        sendView.isEnabled = item.send
        sendView.setOnClickListener {
            navigation?.add(SendScreen.newInstance(jettonAddress = item.tokenAddress))
        }
        receiveView.setOnClickListener {
            navigation?.add(QRScreen.newInstance(item.walletAddress, item.token, item.walletType))
        }
        swapView.isEnabled = item.swap
        swapView.setOnClickListener {
            navigation?.add(SwapScreenNew.newInstance(item.token.symbol))
        }
        stakeView.setOnClickListener {
            navigation?.add(StakeScreen.newInstance(item.tokenAddress))
        }
        unstakeView.setOnClickListener {
            navigation?.add(StakeScreen.newInstance(item.tokenAddress, true))
        }
    }
}