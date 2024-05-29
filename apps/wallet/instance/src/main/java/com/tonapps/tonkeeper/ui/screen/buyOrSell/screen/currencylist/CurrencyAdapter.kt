package com.tonapps.tonkeeper.ui.screen.buyOrSell.screen.currencylist

import android.view.ViewGroup
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.fiatModel.LayoutByCountry
import com.tonapps.tonkeeper.ui.screen.wallet.list.Item
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

class CurrencyAdapter(private val selectedToken: LayoutByCountry?) : BaseListAdapter() {

    var onClickToItem: ((item: LayoutByCountry) -> Unit)? = null

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        return when (viewType) {
            Item.TYPE_CURRENCY_LIST -> CurrencyListHolder(
                parent = parent,
                selectedToken = selectedToken,
                onClickToItem = onClickToItem
            )

            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

}