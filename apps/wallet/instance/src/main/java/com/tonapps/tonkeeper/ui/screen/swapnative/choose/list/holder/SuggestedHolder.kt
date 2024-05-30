package com.tonapps.tonkeeper.ui.screen.swapnative.choose.list.holder

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.ui.screen.swapnative.choose.list.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.BaseListHolder
import uikit.widget.FrescoView

class SuggestedHolder(
    parent: ViewGroup,
    private val onClick: (item: Item) -> Unit
) : BaseListHolder<Item.Suggested>(parent, R.layout.view_cell_suggested_token) {

    private val iconView = findViewById<FrescoView>(R.id.token_icon)
    private val tokenName = findViewById<AppCompatTextView>(R.id.token_title)

    override fun onBind(item: Item.Suggested) {
        itemView.setOnClickListener { onClick(item) }

        item.iconUri?.also { imageUrl ->
            iconView.visibility = View.VISIBLE
            iconView.setImageURI(imageUrl, this)
        }
        tokenName.text = item.symbol


    }


}