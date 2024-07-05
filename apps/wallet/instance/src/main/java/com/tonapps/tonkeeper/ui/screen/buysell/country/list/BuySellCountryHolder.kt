package com.tonapps.tonkeeper.ui.screen.buysell.country.list

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.icon.UIKitIcon
import com.tonapps.uikit.list.BaseListHolder
import uikit.extensions.drawable

class BuySellCountryHolder(
    parent: ViewGroup,
    private val onClick: (item: BuySellCountryItem) -> Unit
): BaseListHolder<BuySellCountryItem>(parent, R.layout.view_country) {

    private val emojiView = findViewById<AppCompatTextView>(R.id.emoji)
    private val titleView = findViewById<AppCompatTextView>(R.id.title)
    private val checkView = findViewById<AppCompatImageView>(R.id.check)

    override fun onBind(item: BuySellCountryItem) {
        itemView.setOnClickListener { onClick(item) }
        itemView.background = item.position.drawable(itemView.context)
        emojiView.text = item.emoji
        titleView.text = item.title
        if (item.selected) {
            checkView.setImageResource(UIKitIcon.ic_donemark_thin_28)
        } else {
            checkView.setImageResource(0)
        }
    }

}
