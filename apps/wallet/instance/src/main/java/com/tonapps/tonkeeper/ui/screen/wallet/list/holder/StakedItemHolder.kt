package com.tonapps.tonkeeper.ui.screen.wallet.list.holder

import android.view.ViewGroup
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.fragment.stake.balance.StakedBalanceFragment
import com.tonapps.tonkeeper.fragment.stake.domain.model.getCryptoBalance
import com.tonapps.tonkeeper.fragment.stake.domain.model.getFiatBalance
import com.tonapps.tonkeeper.fragment.stake.presentation.getIconUrl
import com.tonapps.tonkeeper.ui.screen.wallet.list.Item
import com.tonapps.tonkeeperx.R
import uikit.extensions.setThrottleClickListener
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.item.BaseItemView

class StakedItemHolder(
    parent: ViewGroup,
): Holder<Item.StakedItem>(parent, R.layout.view_wallet_staked_item) {

    private val iconBig: SimpleDraweeView = findViewById(R.id.view_wallet_staked_item_icon_big)
    private val iconSmall: SimpleDraweeView = findViewById(R.id.view_wallet_staked_item_icon_small)
    private val poolName: TextView = findViewById(R.id.view_wallet_staked_item_pool_name)
    private val balanceCryptoTextView: TextView = findViewById(R.id.view_wallet_staked_item_balance_crypto)
    private val balanceFiatTextView: TextView = findViewById(R.id.view_wallet_staked_item_balance_fiat)
    private val baseItemView: BaseItemView = itemView as BaseItemView
    override fun onBind(item: Item.StakedItem) {
        baseItemView.position = item.position
        baseItemView.setThrottleClickListener {
            context.navigation?.add(
                StakedBalanceFragment.newInstance(item.balance)
            )
        }
        iconBig.setImageResource(com.tonapps.wallet.api.R.drawable.ic_ton_with_bg)
        iconSmall.setImageURI(item.balance.pool.serviceType.getIconUrl())
        poolName.text = item.balance.pool.name
        val balanceFiat = item.balance.getFiatBalance()
        val balanceCrypto = item.balance.getCryptoBalance()
        balanceCryptoTextView.text = CurrencyFormatter.format(balanceCrypto, 2)
        balanceFiatTextView.text = CurrencyFormatter.format(
            item.balance.currency.code,
            balanceFiat
        )
    }
}