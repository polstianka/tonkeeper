package com.tonapps.tonkeeper.ui.screen.battery.list.holder

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.ui.screen.battery.list.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.data.battery.entity.BatterySupportedTransaction
import com.tonapps.wallet.localization.Localization
import com.tonapps.wallet.localization.Plurals
import uikit.extensions.drawable
import uikit.widget.SwitchView

class SupportedTransactionHolder(
    parent: ViewGroup,
    private val toggleTransaction: (transaction: BatterySupportedTransaction, enabled: Boolean) -> Unit
) :
    Holder<Item.SupportedTransaction>(parent, R.layout.view_battery_settings) {

    private val chevronView = itemView.findViewById<AppCompatImageView>(R.id.chevron)
    private val switchView = findViewById<SwitchView>(R.id.enabled)
    private val titleView = itemView.findViewById<AppCompatTextView>(R.id.title)
    private val subtitleView = itemView.findViewById<AppCompatTextView>(R.id.subtitle)

    private val transactionNameMap = mapOf(
        BatterySupportedTransaction.NFT to Localization.battery_nft,
        BatterySupportedTransaction.SWAP to Localization.battery_swap,
        BatterySupportedTransaction.JETTON to Localization.battery_jetton,
    )
    private val transactionSingleNameMap = mapOf(
        BatterySupportedTransaction.NFT to Localization.battery_transfer_single,
        BatterySupportedTransaction.SWAP to Localization.battery_swap_single,
        BatterySupportedTransaction.JETTON to Localization.battery_transfer_single,
    )

    init {
        chevronView.visibility = View.GONE
    }

    override fun onBind(item: Item.SupportedTransaction) {
        itemView.background = item.position.drawable(context)
        itemView.setOnClickListener {
            toggleTransaction(item.supportedTransaction, !item.enabled)
        }
        itemView.isClickable = item.showToggle

        titleView.text = context.getString(transactionNameMap[item.supportedTransaction]!!)
            .replaceFirstChar { it.uppercase() }

        subtitleView.text = context.resources.getQuantityString(
            Plurals.battery_charges_per_action,
            item.changes,
            item.changes,
            context.getString(transactionSingleNameMap[item.supportedTransaction]!!)
        )

        if (item.showToggle) {
            switchView.visibility = View.VISIBLE
            switchView.doCheckedChanged = { checked, byUser ->
                if (byUser) {
                    toggleTransaction(item.supportedTransaction, checked)
                }
            }
            switchView.setChecked(item.enabled, false)
        } else {
            switchView.visibility = View.GONE
        }
    }
}