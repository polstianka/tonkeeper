package com.tonapps.tonkeeper.ui.screen.wallet.list.holder

import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.tonapps.blockchain.Coin
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.fragment.stake.presentation.getIconUrl
import com.tonapps.tonkeeper.ui.screen.wallet.list.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.api.entity.TokenEntity
import uikit.extensions.setThrottleClickListener
import uikit.widget.item.BaseItemView

class StakedItemHolder(
    parent: ViewGroup,
): Holder<Item.StakedItem>(parent, R.layout.view_wallet_staked_item) {

    private val iconBig: SimpleDraweeView = findViewById(R.id.view_wallet_staked_item_icon_big)
    private val iconSmall: SimpleDraweeView = findViewById(R.id.view_wallet_staked_item_icon_small)
    private val poolName: TextView = findViewById(R.id.view_wallet_staked_item_pool_name)
    private val balanceCrypto: TextView = findViewById(R.id.view_wallet_staked_item_balance_crypto)
    private val balanceFiat: TextView = findViewById(R.id.view_wallet_staked_item_balance_fiat)
    private val baseItemView: BaseItemView = itemView as BaseItemView
    override fun onBind(item: Item.StakedItem) {
        baseItemView.position = item.position
        baseItemView.setThrottleClickListener { Log.wtf("###", "onItemClicked: $item") }
        iconBig.setImageResource(com.tonapps.wallet.api.R.drawable.ic_ton_with_bg)
        iconSmall.setImageURI(item.balance.pool.serviceType.getIconUrl())
        poolName.text = item.balance.pool.name
        balanceCrypto.text = CurrencyFormatter.format(
            "", item.balance.balance.movePointLeft(item.balance.asset?.decimals ?: Coin.TON_DECIMALS)
        )
        balanceFiat.text = "todo"
    }
}