package com.tonapps.tonkeeper.ui.screen.wallet.list.holder

import android.view.View
import android.view.ViewGroup
import com.tonapps.tonkeeper.extensions.fiat
import com.tonapps.tonkeeper.extensions.openCamera
import com.tonapps.tonkeeper.extensions.sendCoin
import com.tonapps.tonkeeper.extensions.stake
import com.tonapps.tonkeeper.extensions.swap
import com.tonapps.tonkeeper.ui.screen.qr.QRScreen
import com.tonapps.tonkeeper.ui.screen.wallet.list.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.data.account.WalletType
import uikit.navigation.Navigation

class ActionsHolder(parent: ViewGroup): Holder<Item.Actions>(parent, R.layout.view_wallet_actions) {

    private val sendView = findViewById<View>(R.id.send)
    private val receiveView = findViewById<View>(R.id.receive)
    private val buyOrSellView = findViewById<View>(R.id.buy_or_sell)
    private val swapView = findViewById<View>(R.id.swap)
    private val scanView = findViewById<View>(R.id.scan)
    private val stakeView = findViewById<View>(R.id.stake)
    private val navigation = Navigation.from(context)

    init {
        sendView.setOnClickListener { navigation?.sendCoin() }
        buyOrSellView.setOnClickListener { navigation?.fiat() }
        scanView.setOnClickListener { navigation?.openCamera() }
        swapView.setOnClickListener { navigation?.swap() }
        stakeView.setOnClickListener { navigation?.stake() }
    }

    override fun onBind(item: Item.Actions) {
        receiveView.setOnClickListener {
            navigation?.add(QRScreen.newInstance(item.address, item.token, item.walletType))
        }

        stakeView.isEnabled = item.walletType != WalletType.Watch
        swapView.isEnabled = item.walletType != WalletType.Watch && !item.disableSwap
        sendView.isEnabled = item.walletType != WalletType.Watch
        scanView.isEnabled = item.walletType != WalletType.Watch
        buyOrSellView.isEnabled = item.walletType != WalletType.Testnet && !item.disableSwap
    }

}