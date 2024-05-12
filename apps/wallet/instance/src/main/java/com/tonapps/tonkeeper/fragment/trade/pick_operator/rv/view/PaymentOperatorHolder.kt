package com.tonapps.tonkeeper.fragment.trade.pick_operator.rv.view

import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.view.isVisible
import com.facebook.drawee.view.SimpleDraweeView
import com.tonapps.tonkeeper.fragment.trade.pick_operator.rv.model.PaymentOperatorListItem
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.BaseListHolder
import uikit.widget.item.BaseItemView

class PaymentOperatorHolder(
    parent: ViewGroup,
    private val onClick: (PaymentOperatorListItem) -> Unit,
) : BaseListHolder<PaymentOperatorListItem>(
    parent,
    R.layout.view_payment_operator
) {

    private val baseItemView: BaseItemView
        get() = itemView as BaseItemView
    private val iconView = findViewById<SimpleDraweeView>(R.id.view_payment_operator_icon)
    private val titleView = findViewById<TextView>(R.id.view_payment_operator_title)
    private val chipView = findViewById<View>(R.id.view_payment_operator_chip)
    private val descriptionView = findViewById<TextView>(R.id.view_payment_operator_description)
    private val radioButton = findViewById<RadioButton>(R.id.view_payment_operator_radiobutton)

    override fun onBind(item: PaymentOperatorListItem) {
        baseItemView.position = item.position
        iconView.setImageURI(item.iconUrl)
        titleView.text = item.title
        chipView.isVisible = item.isBest
        descriptionView.text = item.rate
        radioButton.isChecked = item.isPicked
        itemView.setOnClickListener { onClick(item) }
    }
}