package com.tonapps.wallet.data.battery.entity

import android.os.Parcelable
import com.tonapps.icu.Coins
import kotlinx.parcelize.Parcelize

@Parcelize
data class BatteryBalanceEntity(
    val balance: Coins,
    val reservedBalance: Coins,
) : Parcelable {

    companion object {
        val Empty = BatteryBalanceEntity(
            balance = Coins.ZERO,
            reservedBalance = Coins.ZERO
        )
    }
}