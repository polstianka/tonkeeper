package com.tonapps.tonkeeper.ui.screen.wallet.list.holder

import android.view.View
import android.view.ViewGroup
import com.tonapps.tonkeeper.dialog.trade.TradeDialog
import com.tonapps.tonkeeper.extensions.openCamera
import com.tonapps.tonkeeper.extensions.sendCoin
import com.tonapps.tonkeeper.ui.screen.qr.QRScreen
import com.tonapps.tonkeeper.ui.screen.swap.SwapScreen
import com.tonapps.tonkeeper.ui.screen.wallet.list.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.WalletType
import uikit.navigation.Navigation

class ActionsHolder(parent: ViewGroup) : Holder<Item.Actions>(parent, R.layout.view_wallet_actions) {
    private val sendView = findViewById<View>(R.id.send)
    private val receiveView = findViewById<View>(R.id.receive)
    private val buyOrSellView = findViewById<View>(R.id.buy_or_sell)
    private val swapView = findViewById<View>(R.id.swap)
    private val scanView = findViewById<View>(R.id.scan)
    private val navigation = Navigation.from(context)

    init {
        sendView.setOnClickListener { navigation?.sendCoin() }
        buyOrSellView.setOnClickListener {
            navigation?.add(TradeDialog())
        }
        scanView.setOnClickListener { navigation?.openCamera() }
    }

    override fun onBind(item: Item.Actions) {
        receiveView.setOnClickListener {
            navigation?.add(QRScreen.newInstance(item.address, item.token, item.walletType))
        }
        swapView.setOnClickListener {
            navigation?.add(SwapScreen.newInstance(item.swapUri, item.address, TokenEntity.TON.address))
        }

        swapView.isEnabled = item.walletType == WalletType.Default && !item.disableSwap
        sendView.isEnabled = item.walletType != WalletType.Watch
        scanView.isEnabled = item.walletType != WalletType.Watch
        buyOrSellView.isEnabled = true // TODO V item.walletType != WalletType.Testnet && !item.disableSwap
    }
}
