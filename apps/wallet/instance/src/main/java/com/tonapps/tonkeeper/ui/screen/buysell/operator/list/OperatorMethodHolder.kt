package com.tonapps.tonkeeper.ui.screen.buysell.operator.list

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.facebook.drawee.view.SimpleDraweeView
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.ListCell
import uikit.extensions.drawable

class OperatorMethodHolder(
    parent: ViewGroup,
    private val onClick: (item: OperatorMethodItem) -> Unit
) : BaseListHolder<OperatorMethodItem>(parent, R.layout.view_fiat_operator) {

    private val iconView = findViewById<SimpleDraweeView>(R.id.icon)
    private val titleView = findViewById<AppCompatTextView>(R.id.title)
    private val subtitleView = findViewById<AppCompatTextView>(R.id.subtitle)
    private val bestBadge = findViewById<AppCompatTextView>(R.id.best_badge)

    override fun onBind(item: OperatorMethodItem) {
        itemView.background = item.position.drawable(itemView.context)
        iconView.setImageURI(item.iconUrl)
        itemView.setOnClickListener { onClick(item) }

        titleView.text = item.title
        subtitleView.text = item.subtitle
        item.rate?.also { rate ->
            val rateNumber = CurrencyFormatter.format(value = rate.rate)
            subtitleView.text = "${rateNumber} ${rate.currency} for 1 TON"
        }

        bestBadge.visibility = if (item.position == ListCell.Position.FIRST) {
            View.VISIBLE
        } else View.GONE
    }

}