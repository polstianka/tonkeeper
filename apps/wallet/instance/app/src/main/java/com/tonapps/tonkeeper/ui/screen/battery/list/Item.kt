package com.tonapps.tonkeeper.ui.screen.battery.list

import android.net.Uri
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.settings.BatteryTransaction

sealed class Item(type: Int) : BaseListItem(type) {

    companion object {
        const val TYPE_BATTERY = 0
        const val TYPE_RECHARGE_METHOD = 1
        const val TYPE_GIFT = 2
        const val TYPE_SPACE = 3
        const val TYPE_SETTINGS = 4
        const val TYPE_SUPPORTED_TRANSACTION = 5
        const val TYPE_SETTINGS_HEADER = 6
    }

    data class Battery(
        val balance: Float,
        val beta: Boolean,
        val changes: Int,
        val formattedChanges: CharSequence,
    ) : Item(TYPE_BATTERY)

    data class RechargeMethod(
        val position: ListCell.Position,
        val currency: String,
        val image: Uri,
    ) : Item(TYPE_RECHARGE_METHOD)

    data class Gift(
        val position: ListCell.Position,
    ) : Item(TYPE_GIFT)

    class Space : Item(TYPE_SPACE)

    data class Settings(
        val supportedTransactions: List<BatteryTransaction>,
    ) : Item(TYPE_SETTINGS)

    data class SupportedTransaction(
        val wallet: WalletEntity,
        val position: ListCell.Position,
        val supportedTransaction: BatteryTransaction,
        val enabled: Boolean,
        val changes: Int,
        val showToggle: Boolean
    ) : Item(TYPE_SUPPORTED_TRANSACTION) {

        val accountId: String
            get() = wallet.accountId
    }

    class SettingsHeader : Item(TYPE_SETTINGS_HEADER)
}