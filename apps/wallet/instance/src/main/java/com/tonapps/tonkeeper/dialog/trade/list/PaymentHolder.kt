package com.tonapps.tonkeeper.dialog.trade.list

import android.view.ViewGroup
import android.widget.RadioButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.HtmlCompat
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.BaseListHolder
import uikit.extensions.drawable

class PaymentHolder(
    parent: ViewGroup,
    private val onClick: (item: PaymentListItem) -> Unit,
) : BaseListHolder<PaymentListItem>(parent, R.layout.view_payment_type) {
    private val titleView = findViewById<AppCompatTextView>(R.id.title)
    private val radio = findViewById<RadioButton>(R.id.radio)
    private val paymentTypeImage = findViewById<AppCompatImageView>(R.id.payment_type_image)

    override fun onBind(item: PaymentListItem) {
        itemView.background = item.position.drawable(itemView.context)
        itemView.setOnClickListener { onClick(item) }
        radio.isChecked = item.isSelected
        titleView.text = HtmlCompat.fromHtml(item.title, HtmlCompat.FROM_HTML_MODE_COMPACT)
        paymentTypeImage.setImageDrawable(context.getDrawable(item.iconIds[0]))
    }
}
