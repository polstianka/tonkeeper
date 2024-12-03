package com.tonapps.tonkeeper.ui.screen.wallet.main.list.holder

import android.content.res.Resources
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.ui.screen.card.entity.CardScreenPath
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.data.core.HIDDEN_BALANCE
import com.tonapps.wallet.localization.Localization
import uikit.extensions.dp

class CardHolder(
    parent: ViewGroup,
    private val openCards: (path: CardScreenPath?) -> Unit
) : Holder<Item.Card>(parent, R.layout.view_card_item) {


    private val titleView = findViewById<AppCompatTextView>(R.id.title)
    private val subtitleView = findViewById<AppCompatTextView>(R.id.subtitle)
    private val numberView = findViewById<AppCompatTextView>(R.id.number)
    private val largeContainerView = findViewById<ViewGroup>(R.id.large_container)
    private val largeTitleView = findViewById<AppCompatTextView>(R.id.large_title)
    private val largeSubtitleView = findViewById<AppCompatTextView>(R.id.large_subtitle)

    override fun onBind(item: Item.Card) {
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels

        val layoutParams = itemView.layoutParams
        layoutParams.width = if (item.isSingle) {
            screenWidth - 32.dp
        } else {
            (screenWidth - 40.dp) / 2
        }

        largeTitleView.text = context.getString(Localization.bank_card, item.lastFourDigits)
        largeSubtitleView.text = context.getString(item.kindResId)
        largeContainerView.visibility = if (item.isSingle) {
            AppCompatTextView.VISIBLE
        } else {
            AppCompatTextView.GONE
        }

        titleView.text = if (item.hiddenBalance) {
            HIDDEN_BALANCE
        } else {
            item.title
        }

        subtitleView.text = if (item.hiddenBalance) {
            HIDDEN_BALANCE
        } else {
            item.subtitle
        }

        numberView.text = item.lastFourDigits

        itemView.setOnClickListener {
            openCards(item.path)
        }
    }
}