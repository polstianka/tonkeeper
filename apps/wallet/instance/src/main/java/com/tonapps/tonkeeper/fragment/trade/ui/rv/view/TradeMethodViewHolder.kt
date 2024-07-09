package com.tonapps.tonkeeper.fragment.trade.ui.rv.view

import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.tonapps.tonkeeper.fragment.trade.ui.rv.model.ExchangeMethodListItem
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.BaseListHolder
import uikit.extensions.setThrottleClickListener
import uikit.widget.item.BaseItemView

class TradeMethodViewHolder(
    parent: ViewGroup,
    val onItemClicked: (ExchangeMethodListItem) -> Unit
) : BaseListHolder<ExchangeMethodListItem>(parent, R.layout.view_trade_method) {

    private val baseItemView: BaseItemView
        get() = itemView as BaseItemView
    private val radioButton = findViewById<RadioButton>(R.id.view_trade_method_radio_button)
    private val titleTextView = findViewById<TextView>(R.id.view_trade_method_title_view)
    private val draweeView = findViewById<SimpleDraweeView>(R.id.view_trade_method_drawee_view)

    override fun onBind(item: ExchangeMethodListItem) {
        baseItemView.position = item.position
        radioButton.isChecked = item.isChecked
        titleTextView.text = item.title
        draweeView.setImageURI(item.iconUrl)
        itemView.setThrottleClickListener { onItemClicked(item) }
    }
}