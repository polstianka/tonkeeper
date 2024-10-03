package com.tonapps.tonkeeper.ui.screen.battery.recharge.list

import com.tonapps.tonkeeper.ui.screen.battery.recharge.entity.RechargePackType
import com.tonapps.tonkeeper.ui.screen.battery.refill.entity.PromoState
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.settings.BatteryTransaction

sealed class Item(type: Int) : BaseListItem(type) {

    companion object {
        const val TYPE_RECHARGE_PACK = 0
        const val TYPE_CUSTOM_AMOUNT = 1
        const val TYPE_SPACE = 2
        const val TYPE_AMOUNT = 3
        const val TYPE_ADDRESS = 4
        const val TYPE_BUTTON = 5
        const val TYPE_PROMO = 6
    }

    data class RechargePack(
        val position: ListCell.Position,
        val packType: RechargePackType,
        val charges: Int,
        val formattedAmount: CharSequence,
        val formattedFiatAmount: CharSequence,
        val batteryLevel: Float,
        val isEnabled: Boolean,
        val selected: Boolean,
        val transactions: Map<BatteryTransaction, Int>
    ) : Item(TYPE_RECHARGE_PACK)

    data class CustomAmount(
        val position: ListCell.Position,
        val selected: Boolean,
    ) : Item(TYPE_CUSTOM_AMOUNT)

    data class Amount(
        val symbol: String,
        val decimals: Int,
        val formattedRemaining: CharSequence,
        val formattedMinAmount: CharSequence,
        val isInsufficientBalance: Boolean,
        val isLessThanMin: Boolean,
        val formattedCharges: CharSequence,
    ) : Item(TYPE_AMOUNT)

    data class Address(
        val loading: Boolean,
        val error: Boolean
    ) : Item(TYPE_ADDRESS)

    data class Button(
        val isEnabled: Boolean,
    ) : Item(TYPE_BUTTON)

    data class Promo(
        val promoState: PromoState,
    ) : Item(TYPE_PROMO) {

        val appliedPromo: String
            get() = (promoState as? PromoState.Applied)?.appliedPromo ?: ""

        val isLoading: Boolean
            get() = promoState is PromoState.Loading

        val isError: Boolean
            get() = promoState is PromoState.Error
    }

    data object Space : Item(TYPE_SPACE)
}