package com.tonapps.tonkeeper.fragment.swap.token.suggestions

import android.view.View
import com.tonapps.tonkeeper.fragment.swap.model.TokenInfo
import com.tonapps.tonkeeper.fragment.swap.view.TokenChipView
import com.tonapps.uikit.list.BaseListHolder

class SuggestionTokenHolder(view: View, private val onClick: (item: TokenInfo) -> Unit): BaseListHolder<SuggestionTokenItem>(view) {
    override fun onBind(item: SuggestionTokenItem) {
        itemView.setOnClickListener { onClick.invoke(item.tokenInfo) }
        (itemView as TokenChipView).token = item.tokenInfo
    }
}
