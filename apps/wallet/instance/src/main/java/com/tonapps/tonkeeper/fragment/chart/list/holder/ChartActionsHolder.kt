package com.tonapps.tonkeeper.fragment.chart.list.holder

import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateMargins
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.extensions.CONTEST_ENABLE_PURCHASE
import com.tonapps.tonkeeper.extensions.CONTEST_ENABLE_STAKE
import com.tonapps.tonkeeper.extensions.CONTEST_ENABLE_SWAP
import com.tonapps.tonkeeper.extensions.buyCoins
import com.tonapps.tonkeeper.extensions.openCamera
import com.tonapps.tonkeeper.extensions.sendCoin
import com.tonapps.tonkeeper.extensions.stakeCoins
import com.tonapps.tonkeeper.extensions.swapTokens
import com.tonapps.tonkeeper.fragment.chart.list.ChartItem
import com.tonapps.tonkeeper.ui.screen.qr.QRScreen
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.WalletType
import uikit.navigation.Navigation

class ChartActionsHolder(
    parent: ViewGroup
): ChartHolder<ChartItem.Actions>(parent, R.layout.view_wallet_actions) {
    private val navigation = Navigation.from(context)
    private val sendView = findViewById<View>(R.id.send)
    private val receiveView = findViewById<View>(R.id.receive)
    private val swapView = findViewById<View>(R.id.swap)
    private val buyOrSellView = findViewById<View>(R.id.buy_or_sell)
    private val stakeView = findViewById<View>(R.id.stake)
    private val scanView = findViewById<View>(R.id.scan)

    init {
        val offsetVertical = context.resources.getDimensionPixelSize(uikit.R.dimen.offsetMedium)
        (itemView.layoutParams as RecyclerView.LayoutParams).updateMargins(top = offsetVertical, bottom = offsetVertical)
        sendView.setOnClickListener { navigation?.sendCoin() }
        scanView.setOnClickListener { navigation?.openCamera() }

        buyOrSellView.visibility = View.GONE
        stakeView.visibility = View.GONE
        scanView.visibility = View.GONE
    }

    override fun onBind(item: ChartItem.Actions) {
        val tokenEntity = TokenEntity.TON
        receiveView.setOnClickListener {
            navigation?.add(QRScreen.newInstance(item.address, tokenEntity, item.walletType))
        }
        swapView.setOnClickListener {
            navigation?.swapTokens(item.swapUri, item.address, item.walletType, tokenEntity)
        }
        buyOrSellView.setOnClickListener {
            navigation?.buyCoins(item.address, item.walletType, tokenEntity)
        }
        stakeView.setOnClickListener {
            navigation?.stakeCoins(item.address, item.walletType, tokenEntity)
        }

        // Access legacy implementation via long press
        if (CONTEST_ENABLE_SWAP) {
            swapView.setOnLongClickListener {
                navigation?.swapTokens(item.swapUri, item.address, item.walletType, tokenEntity, legacy = true)
                true
            }
        }
        if (CONTEST_ENABLE_PURCHASE) {
            buyOrSellView.setOnLongClickListener {
                navigation?.buyCoins(item.address, item.walletType, tokenEntity, legacy = true)
                true
            }
        }

        sendView.isEnabled = item.walletType != WalletType.Watch
        scanView.isEnabled = item.walletType != WalletType.Watch

        swapView.isEnabled = item.walletType == WalletType.Default && !item.disableSwap
        buyOrSellView.isEnabled = item.walletType != WalletType.Testnet && !item.disableBuyOrSell
        stakeView.isEnabled = item.walletType == WalletType.Default && !item.disableSwap && CONTEST_ENABLE_STAKE
    }

}