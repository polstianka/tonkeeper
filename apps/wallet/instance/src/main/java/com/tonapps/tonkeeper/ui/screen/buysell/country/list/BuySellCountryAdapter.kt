package com.tonapps.tonkeeper.ui.screen.buysell.country.list

import android.view.ViewGroup

class BuySellCountryAdapter(
    private val onClick: (item: BuySellCountryItem) -> Unit
): com.tonapps.uikit.list.BaseListAdapter() {

    override fun createHolder(parent: ViewGroup, viewType: Int): com.tonapps.uikit.list.BaseListHolder<out com.tonapps.uikit.list.BaseListItem> {
        return BuySellCountryHolder(parent, onClick)
    }
}