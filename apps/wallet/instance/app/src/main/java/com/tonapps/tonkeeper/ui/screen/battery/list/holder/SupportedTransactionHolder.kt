package com.tonapps.tonkeeper.ui.screen.battery.list.holder

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.extensions.capitalized
import com.tonapps.tonkeeper.koin.settingsRepository
import com.tonapps.tonkeeper.ui.screen.battery.list.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.data.settings.BatteryTransaction
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.localization.Localization
import com.tonapps.wallet.localization.Plurals
import uikit.extensions.drawable
import uikit.widget.SwitchView

class SupportedTransactionHolder(parent: ViewGroup): Holder<Item.SupportedTransaction>(parent, R.layout.view_battery_settings) {

    private val settingsRepository: SettingsRepository?
        get() = context.settingsRepository

    private val chevronView = itemView.findViewById<AppCompatImageView>(R.id.chevron)
    private val switchView = findViewById<SwitchView>(R.id.enabled)
    private val titleView = itemView.findViewById<AppCompatTextView>(R.id.title)
    private val subtitleView = itemView.findViewById<AppCompatTextView>(R.id.subtitle)

    private val transactionNameMap = mapOf(
        BatteryTransaction.NFT to Localization.battery_nft,
        BatteryTransaction.SWAP to Localization.battery_swap,
        BatteryTransaction.JETTON to Localization.battery_jetton,
    )
    private val transactionSingleNameMap = mapOf(
        BatteryTransaction.NFT to Localization.battery_transfer_single,
        BatteryTransaction.SWAP to Localization.battery_swap_single,
        BatteryTransaction.JETTON to Localization.battery_transfer_single,
    )

    init {
        chevronView.visibility = View.GONE
    }

    override fun onBind(item: Item.SupportedTransaction) {
        itemView.background = item.position.drawable(context)
        itemView.setOnClickListener {
            batteryEnableTx(item.accountId, item.supportedTransaction, !item.enabled)
        }
        itemView.isClickable = item.showToggle

        titleView.text = context.getString(transactionNameMap[item.supportedTransaction]!!).capitalized

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
                    batteryEnableTx(item.accountId, item.supportedTransaction, checked)
                }
            }
            switchView.setChecked(item.enabled, false)
        } else {
            switchView.visibility = View.GONE
        }
    }

    private fun batteryEnableTx(
        accountId: String,
        type: BatteryTransaction,
        enabled: Boolean
    ) {
        settingsRepository?.batteryEnableTx(accountId, type, !enabled)
    }
}