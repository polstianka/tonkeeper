package com.tonapps.tonkeeper.ui.screen.battery.refill.list.holder

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.ui.screen.battery.refill.list.Item
import com.tonapps.tonkeeper.ui.screen.browser.dapp.DAppScreen
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.localization.Localization
import uikit.extensions.activity
import uikit.extensions.drawable

class RefundHolder(
    parent: ViewGroup,
): Holder<Item.Refund>(parent, R.layout.view_cell_recharge_method) {

    private val refundImageView = itemView.findViewById<AppCompatImageView>(R.id.refund_icon)
    private val titleView = itemView.findViewById<AppCompatTextView>(R.id.title)
    private val subtitleView = itemView.findViewById<AppCompatTextView>(R.id.subtitle)

    override fun onBind(item: Item.Refund) {
        itemView.background = ListCell.Position.SINGLE.drawable(context)
        itemView.setOnClickListener {
            context.activity?.onBackPressed()
            navigation?.add(DAppScreen.newInstance(url = item.refundUrl))
        }
        refundImageView.visibility = View.VISIBLE
        titleView.text = context.getString(Localization.battery_refund_title)
        subtitleView.text = context.getString(Localization.battery_refund_subtitle)
        subtitleView.visibility = View.VISIBLE
    }
}