package com.tonapps.tonkeeper.ui.screen.swapnative.choose.list

import android.view.ViewGroup

class TokenTypeAdapter(
    private val onClick: (item: TokenTypeItem) -> Unit
) : com.tonapps.uikit.list.BaseListAdapter() {

    override fun createHolder(
        parent: ViewGroup,
        viewType: Int
    ): com.tonapps.uikit.list.BaseListHolder<out com.tonapps.uikit.list.BaseListItem> {
        return TokenTypeHolder(parent, onClick)
    }
}