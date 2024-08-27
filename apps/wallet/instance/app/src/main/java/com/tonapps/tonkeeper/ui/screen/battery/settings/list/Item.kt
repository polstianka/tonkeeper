package com.tonapps.tonkeeper.ui.screen.battery.settings.list

import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.settings.BatteryTransaction
import com.tonapps.wallet.localization.Localization

sealed class Item(type: Int) : BaseListItem(type) {

    companion object {
        const val TYPE_SETTINGS_HEADER = 1
        const val TYPE_SUPPORTED_TRANSACTION = 2
    }

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

        val titleRes: Int
            get() = when(supportedTransaction) {
                BatteryTransaction.NFT -> Localization.battery_nft
                BatteryTransaction.SWAP -> Localization.battery_swap
                BatteryTransaction.JETTON -> Localization.battery_jetton
                else -> throw IllegalArgumentException("Unsupported transaction type: $supportedTransaction")
            }

        val typeTitleRes: Int
            get() = when(supportedTransaction) {
                BatteryTransaction.NFT, BatteryTransaction.JETTON -> Localization.battery_transfer_single
                BatteryTransaction.SWAP -> Localization.battery_swap_single
                else -> throw IllegalArgumentException("Unsupported transaction type: $supportedTransaction")
            }
    }

    data object SettingsHeader: Item(TYPE_SETTINGS_HEADER)
}