package com.tonapps.tonkeeper.ui.screen.buyOrSell.screen.currencylist

import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.fiatModel.LayoutByCountry
import com.tonapps.tonkeeper.ui.screen.wallet.list.Item
import com.tonapps.tonkeeper.ui.screen.wallet.list.holder.Holder
import com.tonapps.tonkeeperx.R
import uikit.extensions.drawable

class CurrencyListHolder(
    parent: ViewGroup,
    private val selectedToken: LayoutByCountry?,
    private val onClickToItem: ((item: LayoutByCountry) -> Unit)? = null
) :
    Holder<Item.CurrencyList>(parent, R.layout.currency_list_holder) {

    private val imgSelected = findViewById<ImageView>(R.id.imgSelected)
    private val currency = findViewById<TextView>(R.id.currency)
    private val countryCode = findViewById<TextView>(R.id.countryCode)

    override fun onBind(item: Item.CurrencyList) {
        Log.d("CurrencyListHolder", "item - $item" )
        itemView.background = item.position.drawable(context)
        if (selectedToken != null) {
            imgSelected.isVisible = selectedToken.countryCode == item.countryCode
        }
        itemView.setOnClickListener {
            onClickToItem?.let { it1 ->
                it1(
                    LayoutByCountry(
                        countryCode = item.countryCode,
                        currency = item.currency,
                        methods = item.methods
                    )
                )
            }
        }
        currency.text = item.currency
        countryCode.text = item.countryCode
    }

}