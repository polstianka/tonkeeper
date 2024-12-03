package com.tonapps.tonkeeper.ui.screen.wallet.main.list.holder

import android.view.ViewGroup
import com.tonapps.tonkeeper.ui.screen.card.entity.CardScreenPath
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.Item
import com.tonapps.tonkeeperx.R

class AddCardHolder(
    parent: ViewGroup,
    private val openCards: (path: CardScreenPath?) -> Unit
) : Holder<Item.AddCard>(parent, R.layout.view_card_add_item) {

    override fun onBind(item: Item.AddCard) {

        itemView.setOnClickListener {
            openCards(CardScreenPath.Create)
        }
    }
}