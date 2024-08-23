package com.tonapps.tonkeeper.ui.screen.battery.settings.list.holder

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.extensions.capitalized
import com.tonapps.tonkeeper.koin.settingsRepository
import com.tonapps.tonkeeper.ui.screen.battery.settings.list.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.data.settings.BatteryTransaction
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.localization.Localization
import com.tonapps.wallet.localization.Plurals
import uikit.extensions.drawable
import uikit.widget.SwitchView

class SupportedTransactionHolder(
    parent: ViewGroup
): Holder<Item.SupportedTransaction>(parent, R.layout.view_battery_settings) {

    private val settingsRepository: SettingsRepository?
        get() = context.settingsRepository

    private val chevronView = itemView.findViewById<AppCompatImageView>(R.id.chevron)
    private val switchView = findViewById<SwitchView>(R.id.enabled)
    private val titleView = itemView.findViewById<AppCompatTextView>(R.id.title)
    private val subtitleView = itemView.findViewById<AppCompatTextView>(R.id.subtitle)

    init {
        chevronView.visibility = View.GONE
    }

    override fun onBind(item: Item.SupportedTransaction) {
        itemView.background = item.position.drawable(context)
        if (item.showToggle) {
            itemView.setOnClickListener { switchView.toggle(true) }
            switchView.setChecked(item.enabled, false)
            switchView.visibility = View.VISIBLE
            switchView.doCheckedChanged = { checked, byUser ->
                if (byUser) {
                    settingsRepository?.batteryEnableTx(
                        accountId = item.accountId,
                        type = item.supportedTransaction,
                        enable = checked
                    )
                }
            }
        } else {
            switchView.visibility = View.GONE
        }

        titleView.text = context.getString(item.titleRes).capitalized
        subtitleView.text = context.resources.getQuantityString(
            Plurals.battery_charges_per_action,
            item.changes,
            item.changes,
            getString(item.typeTitleRes)
        )
    }

}