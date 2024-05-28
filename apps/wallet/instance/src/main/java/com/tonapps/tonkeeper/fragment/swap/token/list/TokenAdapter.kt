package com.tonapps.tonkeeper.fragment.swap.token.list

import android.view.ViewGroup

class TokenAdapter(
    private val onClick: (item: TokenItem) -> Unit
): com.tonapps.uikit.list.BaseListAdapter() {

    override fun createHolder(parent: ViewGroup, viewType: Int): com.tonapps.uikit.list.BaseListHolder<out com.tonapps.uikit.list.BaseListItem> {
        return TokenHolder(parent, onClick)
    }
}