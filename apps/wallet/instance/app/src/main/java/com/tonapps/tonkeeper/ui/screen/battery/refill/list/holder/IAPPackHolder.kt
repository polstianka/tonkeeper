package com.tonapps.tonkeeper.ui.screen.battery.refill.list.holder

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.billing.ProductEntity
import com.tonapps.wallet.api.entity.IAPPackageId
import com.tonapps.tonkeeper.ui.screen.battery.refill.list.Item
import com.tonapps.tonkeeper.view.BatteryView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.icon.UIKitIcon
import com.tonapps.wallet.data.settings.BatteryTransaction
import com.tonapps.wallet.localization.Localization
import com.tonapps.wallet.localization.Plurals
import uikit.extensions.drawable
import uikit.extensions.setRightDrawable
import uikit.extensions.getDrawable

class IAPPackHolder(
    parent: ViewGroup,
    private val onPackSelect: (ProductEntity) -> Unit,
) : Holder<Item.IAPPack>(parent, R.layout.view_cell_iap_pack) {

    private val titleView = itemView.findViewById<AppCompatTextView>(R.id.title)
    private val subtitleView = itemView.findViewById<AppCompatTextView>(R.id.subtitle)
    private val detailsView = itemView.findViewById<AppCompatTextView>(R.id.details)
    private val batteryView = itemView.findViewById<BatteryView>(R.id.battery)
    private val amountView = itemView.findViewById<Button>(R.id.amount)

    override fun onBind(item: Item.IAPPack) {
        itemView.background = item.position.drawable(context)

        amountView.text = item.formattedPrice
        amountView.setOnClickListener { onPackSelect(item.product) }
        // amountView.isEnabled = item.isEnabled

        titleView.text = getPackName(item.packType)

        subtitleView.text = context.resources.getQuantityString(
            Plurals.battery_current_charges, item.charges, item.charges
        )
        subtitleView.setRightDrawable(itemView.getDrawable(UIKitIcon.ic_information_circle_16))
        subtitleView.setOnClickListener {
            detailsView.visibility = if (detailsView.visibility == View.VISIBLE) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        detailsView.text = createDetails(item.transactions)

        applyBatteryIcon(item.packType)
    }

    private fun applyBatteryIcon(packType: IAPPackageId) {
        val batteryLevel = when (packType) {
            IAPPackageId.LARGE -> 1f
            IAPPackageId.MEDIUM -> 0.5f
            IAPPackageId.SMALL -> 0.25f
        }
        batteryView.setBatteryLevel(batteryLevel)
    }

    private fun getPackName(packType: IAPPackageId): String {
        return when (packType) {
            IAPPackageId.LARGE -> context.getString(Localization.battery_large_pack)
            IAPPackageId.MEDIUM -> context.getString(Localization.battery_medium_pack)
            IAPPackageId.SMALL -> context.getString(Localization.battery_small_pack)
        }
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
}