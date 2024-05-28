package com.tonapps.tonkeeper.fragment.swap.token.suggestions

import android.view.ViewGroup
import com.tonapps.tonkeeper.fragment.swap.model.TokenInfo
import com.tonapps.tonkeeper.fragment.swap.view.TokenChipView
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

class SuggestionTokenAdapter(
    private val onClick: (item: TokenInfo) -> Unit
): com.tonapps.uikit.list.BaseListAdapter() {

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        val tokenChipView = TokenChipView(context = parent.context)
        tokenChipView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        return SuggestionTokenHolder(tokenChipView, onClick)
    }
}