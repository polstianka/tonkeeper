package com.tonapps.tonkeeper.ui.screen.settings.currency.list

import android.view.ViewGroup
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

class CurrencyAdapter(
    private val onClick: (currency: String) -> Unit
): BaseListAdapter() {

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        return CurrencyHolder(parent, onClick)
    }
}