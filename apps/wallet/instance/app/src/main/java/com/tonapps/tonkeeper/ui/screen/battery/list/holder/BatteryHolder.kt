package com.tonapps.tonkeeper.ui.screen.battery.list.holder

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.ui.screen.battery.list.Item
import com.tonapps.tonkeeper.view.BatteryView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.accentOrangeColor
import com.tonapps.uikit.color.stateList
import com.tonapps.wallet.localization.Localization
import com.tonapps.wallet.localization.Plurals
import uikit.extensions.withAlpha

class BatteryHolder(
    parent: ViewGroup,
    private val openSettings: () -> Unit,
): Holder<Item.Battery>(parent, R.layout.view_battery_icon) {

    private val batteryView = itemView.findViewById<BatteryView>(R.id.battery_view)
    private val betaView = itemView.findViewById<AppCompatTextView>(R.id.beta)
    private val subtitleView = itemView.findViewById<AppCompatTextView>(R.id.battery_subtitle)
    private val supportedTransactionsView = itemView.findViewById<AppCompatTextView>(R.id.supported_transaction)

    override fun onBind(item: Item.Battery) {
        batteryView.setBatteryLevel(item.balance)
        if (item.beta) {
            val color = context.accentOrangeColor
            betaView.setTextColor(color)
            betaView.backgroundTintList = color.withAlpha(.16f).stateList
            betaView.visibility = View.VISIBLE
        }

        if (item.balance > 0) {
            subtitleView.text = context.resources.getQuantityString(Plurals.battery_current_charges, item.changes, item.formattedChanges)
            supportedTransactionsView.visibility = View.GONE
        } else {
            subtitleView.text = context.getString(Localization.battery_refill_subtitle)
            supportedTransactionsView.visibility = View.VISIBLE
            supportedTransactionsView.setOnClickListener { openSettings() }
        }
    }
}