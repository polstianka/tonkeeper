package com.tonapps.tonkeeper.fragment.swap.currency.list

import android.view.ViewGroup
import com.tonapps.tonkeeper.fragment.swap.currency.list.holder.SwapDetailsCellHolder
import com.tonapps.tonkeeper.fragment.swap.currency.list.holder.SwapDetailsDividerHolder
import com.tonapps.tonkeeper.fragment.swap.currency.list.holder.SwapDetailsPriceHolder

class SwapDetailsAdapter(
    private val onClick: (item: SwapDetailsItem) -> Unit
): com.tonapps.uikit.list.BaseListAdapter() {

    override fun createHolder(parent: ViewGroup, viewType: Int): com.tonapps.uikit.list.BaseListHolder<out com.tonapps.uikit.list.BaseListItem> {
        return when (viewType) {
            SwapDetailsItem.TYPE_HEADER -> SwapDetailsPriceHolder(parent, onClick)
            SwapDetailsItem.TYPE_ACTIONS -> SwapDetailsCellHolder(parent, onClick)
            SwapDetailsItem.TYPE_DIVIDER -> SwapDetailsDividerHolder(parent)
            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }
}