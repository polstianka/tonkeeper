package com.tonapps.tonkeeper.ui.screen.battery.list.holder

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.ui.screen.battery.list.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.settings.BatteryTransaction
import com.tonapps.wallet.localization.Localization
import uikit.extensions.drawable

class SettingsHolder(
    parent: ViewGroup,
    private val openSettings: () -> Unit
): Holder<Item.Settings>(parent, R.layout.view_battery_settings) {

    private val subtitleView = itemView.findViewById<AppCompatTextView>(R.id.subtitle)

    override fun onBind(item: Item.Settings) {
        itemView.background = ListCell.Position.SINGLE.drawable(context)
        itemView.setOnClickListener { openSettings() }
        if (item.supportedTransactions.isEmpty()) {
            subtitleView.visibility = View.GONE
        } else {
            subtitleView.visibility = View.VISIBLE
            subtitleView.text = context.getString(
                Localization.battery_will_be_paid,
                getSupportedTransactionText(item.supportedTransactions)
            )
        }
    }

    private fun getSupportedTransactionText(supportedTransactions: List<BatteryTransaction>): String {
        return supportedTransactions.joinToString(", ") {
            context.getString(supportedTransactionMap[it]!!)
        }
    }

    companion object {

        private val supportedTransactionMap = mapOf(
            BatteryTransaction.NFT to Localization.battery_nft,
            BatteryTransaction.SWAP to Localization.battery_swap,
            BatteryTransaction.JETTON to Localization.battery_jetton,
        )
    }
}