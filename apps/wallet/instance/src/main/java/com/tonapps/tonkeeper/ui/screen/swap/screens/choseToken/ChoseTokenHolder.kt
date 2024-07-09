package com.tonapps.tonkeeper.ui.screen.swap.screens.choseToken

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.tonapps.tonkeeper.ui.screen.swap.model.assets.Asset
import com.tonapps.tonkeeper.ui.screen.swap.fromNanocoinToCoin
import com.tonapps.tonkeeper.ui.screen.wallet.list.Item
import com.tonapps.tonkeeper.ui.screen.wallet.list.holder.Holder
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.buttonTertiaryBackgroundDisabledColor
import uikit.extensions.drawable
import uikit.widget.FrescoView

class ChoseTokenHolder(
    parent: ViewGroup,
    val clickToItem: ((token: Asset) -> Unit)?,
    private val selectedToken: Asset?
) :
    Holder<Item.ChoseToken>(parent, R.layout.view_cell_jetton) {

    private val iconView = findViewById<FrescoView>(R.id.icon)
    private val titleView = findViewById<AppCompatTextView>(R.id.title)
    private val rateView = findViewById<AppCompatTextView>(R.id.rate)
    private val balanceView = findViewById<AppCompatTextView>(R.id.balance)
    private val balanceFiatView = findViewById<AppCompatTextView>(R.id.balance_currency)


    override fun onBind(item: Item.ChoseToken) {
        itemView.setOnClickListener {
            clickToItem?.let { it1 ->
                it1(
                    Asset(
                        contract_address = item.contract_address,
                        symbol = item.symbol,
                        display_name = item.display_name,
                        image_url = item.image_url,
                        decimals = item.decimals,
                        kind = item.kind,
                        wallet_address = item.wallet_address,
                        balance = item.balance,
                        third_party_usd_price = item.third_party_usd_price,
                        dex_usd_price = item.dex_usd_price
                    )
                )
            }
        }
        if (selectedToken != null) {
            if (selectedToken.contract_address == item.contract_address) {
                itemView.background =
                    item.position.drawable(context, context.buttonTertiaryBackgroundDisabledColor)
            } else {
                itemView.background = item.position.drawable(context)
            }
        } else {
            itemView.background = item.position.drawable(context)
        }

        iconView.setImageURI(item.image_url.toUri(), this)
        titleView.text = item.symbol
        rateView.text = item.display_name
        if (item.balance != null) {
            balanceView.isVisible = true
            balanceView.text = item.balance.toLong().fromNanocoinToCoin().toString()
        } else {
            balanceView.isVisible = false
            balanceFiatView.text = "0"
        }
    }
}