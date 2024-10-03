package com.tonapps.tonkeeper.ui.screen.battery.recharge.entity

import androidx.annotation.StringRes
import com.tonapps.icu.Coins
import com.tonapps.wallet.data.core.entity.SignRequestEntity

sealed class BatteryRechargeEvent {
    data class Sign(val request: SignRequestEntity, val forceRelayer: Boolean) : BatteryRechargeEvent()
    data object Error : BatteryRechargeEvent()
    data class MaxAmountError(val maxAmount: Coins, val currency: String) : BatteryRechargeEvent()
}
