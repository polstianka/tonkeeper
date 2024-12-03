package com.tonapps.tonkeeper.ui.screen.wallet.main.list

import android.view.ViewGroup
import com.tonapps.tonkeeper.ui.screen.card.entity.CardScreenPath
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.holder.AddCardHolder
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.holder.CardHolder
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

class CardsAdapter(private val openCards: (path: CardScreenPath?) -> Unit) : BaseListAdapter() {

    override fun createHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseListHolder<out BaseListItem> {
        return when (viewType) {
            Item.TYPE_CARD -> CardHolder(parent, openCards)
            Item.TYPE_ADD_CARD -> AddCardHolder(parent, openCards)
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

}