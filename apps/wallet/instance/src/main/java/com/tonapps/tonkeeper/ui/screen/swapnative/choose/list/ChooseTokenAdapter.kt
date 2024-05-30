package com.tonapps.tonkeeper.ui.screen.swapnative.choose.list

import android.view.ViewGroup
import com.tonapps.tonkeeper.ui.screen.swapnative.choose.list.holder.SuggestedHolder
import com.tonapps.tonkeeper.ui.screen.swapnative.choose.list.holder.TitleHolder
import com.tonapps.tonkeeper.ui.screen.swapnative.choose.list.holder.TokenTypeHolder

class ChooseTokenAdapter(
    private val onClick: (item: Item) -> Unit
) : com.tonapps.uikit.list.BaseListAdapter() {

    override fun createHolder(
        parent: ViewGroup,
        viewType: Int
    ): com.tonapps.uikit.list.BaseListHolder<out com.tonapps.uikit.list.BaseListItem> {
        return when (viewType) {
            Item.TYPE_TOKEN -> TokenTypeHolder(parent, onClick)
            Item.TYPE_SUGGESTED -> SuggestedHolder(parent, onClick)
            Item.TYPE_TITLE -> TitleHolder(parent, onClick)
            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }
}