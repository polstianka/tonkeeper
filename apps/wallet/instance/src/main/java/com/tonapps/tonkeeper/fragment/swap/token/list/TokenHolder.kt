package com.tonapps.tonkeeper.fragment.swap.token.list

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.backgroundContentColor
import com.tonapps.uikit.color.backgroundContentTintColor
import com.tonapps.uikit.list.BaseListHolder
import uikit.extensions.drawable
import uikit.widget.FrescoView

class TokenHolder(
    parent: ViewGroup,
    private val onClick: (item: TokenItem) -> Unit
): BaseListHolder<TokenItem>(parent, R.layout.view_cell_token) {

    private val iconView = findViewById<FrescoView>(R.id.icon)
    private val symbolView = findViewById<AppCompatTextView>(R.id.symbol)
    private val nameView = findViewById<AppCompatTextView>(R.id.name)
    private val balanceTonView = findViewById<AppCompatTextView>(R.id.balance_ton)
    private val balanceFiatView = findViewById<AppCompatTextView>(R.id.balance_fiat)
    private val tagView = findViewById<View>(R.id.tag)

    override fun onBind(item: TokenItem) {
        itemView.setOnClickListener { onClick(item) }
        itemView.background = item.position.drawable(
            context = itemView.context,
            backgroundColor = if (item.selected) {
                context.backgroundContentTintColor
            } else {
                context.backgroundContentColor
            }
        )

        val token = item.tokenInfo
        iconView.setImageURI(token.iconUri)
        symbolView.text = token.symbol
        nameView.text = token.name
        balanceTonView.text = token.balance
        balanceFiatView.text = token.balanceFiat
        tagView.isVisible = token.tonTag
    }

}
