package com.tonapps.tonkeeper.ui.screen.wallet.list.holder

import android.view.View
import android.view.ViewGroup
import com.tonapps.tonkeeper.extensions.CONTEST_ENABLE_PURCHASE
import com.tonapps.tonkeeper.extensions.CONTEST_ENABLE_STAKE
import com.tonapps.tonkeeper.extensions.CONTEST_ENABLE_SWAP
import com.tonapps.tonkeeper.extensions.buyCoins
import com.tonapps.tonkeeper.extensions.openCamera
import com.tonapps.tonkeeper.extensions.sendCoin
import com.tonapps.tonkeeper.extensions.stakeCoins
import com.tonapps.tonkeeper.extensions.swapTokens
import com.tonapps.tonkeeper.ui.screen.qr.QRScreen
import com.tonapps.tonkeeper.ui.screen.wallet.list.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.data.account.WalletType
import uikit.navigation.Navigation

class ActionsHolder(parent: ViewGroup): Holder<Item.Actions>(parent, R.layout.view_wallet_actions) {

    private val navigation = Navigation.from(context)
    private val sendView = findViewById<View>(R.id.send)
    private val receiveView = findViewById<View>(R.id.receive)
    private val scanView = findViewById<View>(R.id.scan)
    private val buyOrSellView = findViewById<View>(R.id.buy_or_sell)
    private val swapView = findViewById<View>(R.id.swap)
    private val stakeView = findViewById<View>(R.id.stake)

    init {
        sendView.setOnClickListener { navigation?.sendCoin() }
        scanView.setOnClickListener { navigation?.openCamera() }
    }

    override fun onBind(item: Item.Actions) {
        receiveView.setOnClickListener {
            navigation?.add(QRScreen.newInstance(item.address, item.token, item.walletType))
        }
        swapView.setOnClickListener {
            navigation?.swapTokens(item.swapUri, item.address, item.walletType, item.token)
        }
        buyOrSellView.setOnClickListener {
            navigation?.buyCoins(item.address, item.walletType, item.token)
        }
        stakeView.setOnClickListener {
            navigation?.stakeCoins(item.address, item.walletType, item.token)
        }

        // Access legacy implementation via long press
        if (CONTEST_ENABLE_SWAP) {
            swapView.setOnLongClickListener {
                navigation?.swapTokens(item.swapUri, item.address, item.walletType, item.token, legacy = true)
                true
            }
        }
        if (CONTEST_ENABLE_PURCHASE) {
            buyOrSellView.setOnLongClickListener {
                navigation?.buyCoins(item.address, item.walletType, item.token, legacy = true)
                true
            }
        }

        sendView.isEnabled = item.walletType != WalletType.Watch
        scanView.isEnabled = item.walletType != WalletType.Watch

        swapView.isEnabled = item.walletType == WalletType.Default && !item.disableSwap
        buyOrSellView.isEnabled = item.walletType != WalletType.Testnet && !item.disableSwap
        stakeView.isEnabled = item.walletType == WalletType.Default && !item.disableSwap && CONTEST_ENABLE_STAKE
    }

}