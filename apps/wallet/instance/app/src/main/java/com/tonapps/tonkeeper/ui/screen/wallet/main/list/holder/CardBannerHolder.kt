package com.tonapps.tonkeeper.ui.screen.wallet.main.list.holder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.koin.settingsRepository
import com.tonapps.tonkeeper.ui.screen.card.entity.CardScreenPath
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.Item
import com.tonapps.tonkeeperx.R

class CardBannerHolder(
    parent: ViewGroup,
    private val openCards: (path: CardScreenPath?) -> Unit
) : Holder<Item.CardsBanner>(parent, R.layout.view_cards_banner) {

    private val actionView = findViewById<AppCompatTextView>(R.id.action)
    private val closeView = findViewById<AppCompatImageView>(R.id.close)

    override fun onBind(item: Item.CardsBanner) {
        actionView.setOnClickListener {
            openCards(null)
        }
        closeView.setOnClickListener {
            context.settingsRepository?.cardsDismissed = true
        }
    }
}