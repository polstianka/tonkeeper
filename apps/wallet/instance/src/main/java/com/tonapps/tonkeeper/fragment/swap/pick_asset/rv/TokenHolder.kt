package com.tonapps.tonkeeper.fragment.swap.pick_asset.rv

import android.view.ViewGroup
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.BaseListHolder
import uikit.extensions.setThrottleClickListener
import uikit.widget.item.BaseItemView

class TokenHolder(
    parent: ViewGroup,
    val onItemClicked: (TokenListItem) -> Unit
) : BaseListHolder<TokenListItem>(parent, R.layout.view_token_item) {

    private val baseItemView: BaseItemView = itemView as BaseItemView
    private val icon: SimpleDraweeView = findViewById(R.id.view_token_item_icon)
    private val symbol: TextView = findViewById(R.id.view_token_item_symbol)
    private val amountCrypto: TextView = findViewById(R.id.view_token_item_amount_crypto)
    private val amountFiat: TextView = findViewById(R.id.view_token_item_amount_fiat)
    private val name: TextView = findViewById(R.id.view_token_item_name)
    override fun onBind(item: TokenListItem) {
        baseItemView.position = item.position
        icon.setImageURI(item.iconUrl)
        symbol.text = item.symbol
        amountCrypto.text = item.amountCrypto
        amountFiat.text = item.amountFiat
        name.text = item.name
        baseItemView.setThrottleClickListener { onItemClicked(item) }
    }
}