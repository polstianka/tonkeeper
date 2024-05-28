package com.tonapps.tonkeeper.fragment.chart.list.holder

import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateMargins
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.extensions.openCamera
import com.tonapps.tonkeeper.extensions.sendCoin
import com.tonapps.tonkeeper.fragment.chart.list.ChartItem
import com.tonapps.tonkeeper.ui.screen.buysell.FiatAmountScreen
import com.tonapps.tonkeeper.ui.screen.qr.QRScreen
import com.tonapps.tonkeeper.ui.screen.swap.SwapScreen
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.WalletType
import uikit.navigation.Navigation

class ChartActionsHolder(
    parent: ViewGroup
) : ChartHolder<ChartItem.Actions>(parent, R.layout.view_wallet_actions) {

    private val navigation = Navigation.from(context)
    private val sendView = findViewById<View>(R.id.send)
    private val receiveView = findViewById<View>(R.id.receive)
    private val swapView = findViewById<View>(R.id.swap)
    private val buyOrSellView = findViewById<View>(R.id.buy_or_sell)
    private val scanView = findViewById<View>(R.id.scan)

    init {
        val offsetVertical = context.resources.getDimensionPixelSize(uikit.R.dimen.offsetMedium)
        (itemView.layoutParams as RecyclerView.LayoutParams).updateMargins(
            top = offsetVertical,
            bottom = offsetVertical
        )
        sendView.setOnClickListener { navigation?.sendCoin() }
        buyOrSellView.setOnClickListener { navigation?.add(FiatAmountScreen.newInstance()) }
        scanView.setOnClickListener { navigation?.openCamera() }
    }

    override fun onBind(item: ChartItem.Actions) {
        receiveView.setOnClickListener {
            navigation?.add(QRScreen.newInstance(item.address, TokenEntity.TON, item.walletType))
        }

        swapView.setOnClickListener {
            navigation?.add(SwapScreen.newInstance(item.swapUri, item.address, "TON"))
        }

        swapView.isEnabled = item.walletType == WalletType.Default && !item.disableSwap
        sendView.isEnabled = item.walletType != WalletType.Watch
        scanView.isEnabled = item.walletType != WalletType.Watch
        buyOrSellView.isEnabled = item.walletType != WalletType.Testnet && !item.disableBuyOrSell
    }

}