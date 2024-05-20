package com.tonapps.tonkeeper.fragment.jetton.list.holder

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.tonapps.tonkeeper.extensions.sendCoin
import com.tonapps.tonkeeper.fragment.jetton.list.JettonItem
import com.tonapps.tonkeeper.ui.screen.qr.QRScreen
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.WalletType
import uikit.navigation.Navigation

class JettonActionsStakedHolder(
    parent: ViewGroup
) : JettonHolder<JettonItem.ActionsStaked>(parent, R.layout.view_jetton_actions_staked) {

    private val navigation = Navigation.from(context)
    private val stakeView = findViewById<View>(R.id.stake)
    private val unstakeView = findViewById<View>(R.id.unstake)

    override fun onBind(item: JettonItem.ActionsStaked) {
        stakeView.setOnClickListener {
            navigation?.sendCoin(
                jettonAddress = item.jetton.jetton.address,
            )
        }

        stakeView.isVisible = item.walletType != WalletType.Watch

        unstakeView.setOnClickListener {
            val token = TokenEntity(item.jetton.jetton)
            navigation?.add(QRScreen.newInstance(item.wallet, token, item.walletType))
        }
    }

}