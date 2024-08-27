package com.tonapps.tonkeeper.ui.screen.battery.refill.list

import android.net.Uri
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.settings.BatteryTransaction
import com.tonapps.wallet.data.token.entities.AccountTokenEntity

sealed class Item(type: Int) : BaseListItem(type) {

    companion object {
        const val TYPE_BATTERY = 0
        const val TYPE_SPACE = 1
        const val TYPE_RECHARGE_METHOD = 2
        const val TYPE_GIFT = 3
        const val TYPE_SETTINGS = 4
    }

    data class Battery(
        val balance: Float,
        val beta: Boolean,
        val changes: Int,
        val formattedChanges: CharSequence,
    ) : Item(TYPE_BATTERY)

    data class RechargeMethod(
        val position: ListCell.Position,
        val token: TokenEntity
    ) : Item(TYPE_RECHARGE_METHOD) {

        val symbol: String
            get() = token.symbol

        val imageUri: Uri
            get() = token.imageUri
    }

    data class Gift(
        val position: ListCell.Position,
    ) : Item(TYPE_GIFT)

    data class Settings(
        val supportedTransactions: Array<BatteryTransaction>,
    ) : Item(TYPE_SETTINGS)

    data object Space : Item(TYPE_SPACE)
}