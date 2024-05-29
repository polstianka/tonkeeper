package com.tonapps.tonkeeper.dialog.trade.operator.list

import android.view.ViewGroup
import android.widget.RadioButton
import androidx.appcompat.widget.AppCompatTextView
import com.facebook.drawee.view.SimpleDraweeView
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.BaseListHolder
import uikit.extensions.drawable

class OperatorHolder(
    parent: ViewGroup,
    private val onClick: (item: OperatorListItem) -> Unit,
) : BaseListHolder<OperatorListItem>(parent, R.layout.view_operator_item) {
    private val iconView = findViewById<SimpleDraweeView>(R.id.icon)
    private val titleView = findViewById<AppCompatTextView>(R.id.title)
    private val subtitleView = findViewById<AppCompatTextView>(R.id.subtitle)
    private val radioButton = findViewById<RadioButton>(R.id.radio)

    override fun onBind(item: OperatorListItem) {
        itemView.background = item.position.drawable(itemView.context)
        iconView.setImageURI(item.iconUrl)
        itemView.setOnClickListener { onClick(item) }

        titleView.text = item.title
        subtitleView.text =
            if (item.rate != null) {
                context.getString(
                    com.tonapps.wallet.localization.R.string.rate_for_one_ton,
                    "${CurrencyFormatter.format("", item.rate)} ${item.fiatCurrency}",
                )
            } else {
                item.subtitle
            }
        radioButton.isChecked = item.isSelected
    }
}
