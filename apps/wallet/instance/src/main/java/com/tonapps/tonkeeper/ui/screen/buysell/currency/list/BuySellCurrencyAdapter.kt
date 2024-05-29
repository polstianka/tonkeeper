package com.tonapps.tonkeeper.ui.screen.buysell.currency.list

import android.view.ViewGroup
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

class BuySellCurrencyAdapter(
    private val onClick: (currency: String) -> Unit
): BaseListAdapter() {

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        return BuySellCurrencyHolder(parent, onClick)
    }
}