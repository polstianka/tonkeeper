package com.tonapps.tonkeeper.ui.screen.wallet.main.list.holder

import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.ui.screen.card.entity.CardScreenPath
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.CardsAdapter
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.Item
import com.tonapps.tonkeeperx.R

class CardsHolder(
    private val parent: ViewGroup,
    openCards: (path: CardScreenPath?) -> Unit
) : Holder<Item.Cards>(parent, R.layout.view_cards_list) {

    private val listView = findViewById<RecyclerView>(R.id.cardsRecyclerView)
    private val adapter = CardsAdapter(openCards)

    override fun onBind(item: Item.Cards) {
        listView.layoutManager =
            LinearLayoutManager(parent.context, LinearLayoutManager.HORIZONTAL, false)
        listView.adapter = adapter
        adapter.submitList(item.list)
    }
}