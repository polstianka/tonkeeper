package com.tonapps.tonkeeper.ui.screen.battery.recharge.list.holder

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.ui.screen.battery.recharge.entity.RechargePackType
import com.tonapps.tonkeeper.ui.screen.battery.recharge.list.Item
import com.tonapps.tonkeeper.view.BatteryView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.icon.UIKitIcon
import com.tonapps.wallet.data.settings.BatteryTransaction
import com.tonapps.wallet.localization.Localization
import com.tonapps.wallet.localization.Plurals
import uikit.extensions.drawable
import uikit.extensions.setRightDrawable
import uikit.extensions.getDrawable
import uikit.widget.RadioView

class RechargePackHolder(
    parent: ViewGroup,
    private val onPackSelect: (RechargePackType) -> Unit,
) : Holder<Item.RechargePack>(parent, R.layout.view_cell_recharge_pack) {

    private val titleView = itemView.findViewById<AppCompatTextView>(R.id.title)
    private val subtitleView = itemView.findViewById<AppCompatTextView>(R.id.subtitle)
    private val detailsView = itemView.findViewById<AppCompatTextView>(R.id.details)
    private val batteryView = itemView.findViewById<BatteryView>(R.id.battery)
    private val radioView = itemView.findViewById<RadioView>(R.id.radio)

    override fun onBind(item: Item.RechargePack) {
        itemView.background = item.position.drawable(context)
        itemView.setOnClickListener { onPackSelect(item.packType) }
        itemView.isEnabled = item.isEnabled

        titleView.text = context.resources.getQuantityString(
            Plurals.battery_current_charges, item.charges, item.charges
        )
        titleView.setRightDrawable(itemView.getDrawable(UIKitIcon.ic_information_circle_16))
        titleView.setOnClickListener {
            detailsView.visibility = if (detailsView.visibility == View.VISIBLE) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        subtitleView.text = createSubtitle(item.formattedAmount, item.formattedFiatAmount)

        detailsView.text = createDetails(item.transactions)

        batteryView.setBatteryLevel(item.batteryLevel)

        radioView.checked = item.selected
        radioView.isEnabled = item.isEnabled
        radioView.setOnClickListener { onPackSelect(item.packType) }


    }

    private fun createDetails(transactions: Map<BatteryTransaction, Int>): String {
        val builder = StringBuilder()
        transactions.forEach { (transaction, count) ->
            val titleRes = when (transaction) {
                BatteryTransaction.NFT -> Plurals.battery_nft
                BatteryTransaction.SWAP -> Plurals.battery_swap
                BatteryTransaction.JETTON -> Plurals.battery_jetton
                else -> throw IllegalArgumentException("Unsupported transaction type: $transaction")
            }
            builder.append(context.resources.getQuantityString(titleRes, count, count))
            if (transaction == BatteryTransaction.SWAP) {
                builder.append(",")
            } else if (transaction == BatteryTransaction.NFT) {
                builder.append(" " + context.getString(Localization.or))
            }
            builder.append("\n")
        }
        return builder.toString()
    }

    private fun createSubtitle(tokenAmount: CharSequence, fiatAmount: CharSequence): String {
        val builder = StringBuilder()
        builder.append(tokenAmount)
        builder.append(" · ")
        builder.append(fiatAmount)
        return builder.toString()
    }
}