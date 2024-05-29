package com.tonapps.tonkeeper.ui.adapter.holder

import android.view.ViewGroup
import com.tonapps.tonkeeper.ui.adapter.Item
import com.tonapps.tonkeeper.ui.adapter.view.SuggestedTokenView
import com.tonapps.uikit.list.BaseListHolder

class HolderTokenSuggested(parent: ViewGroup):
    BaseListHolder<Item.TokenSuggested>(SuggestedTokenView(parent.context)) {

    private val tokenView = itemView as SuggestedTokenView

    override fun onBind(item: Item.TokenSuggested) {
        tokenView.setToken(item.symbol, item.iconUri)
        itemView.setOnClickListener {
            item.onClickListener?.invoke()
        }
    }
}