package com.tonapps.wallet.data.battery.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.ton.block.AddrStd

@Parcelize
data class BatteryConfigEntity(
    val excessesAccount: String?,
    val fundReceiver: String?,
    val rechargeMethods: List<RechargeMethodEntity>,
) : Parcelable {

    fun getExcessesAddress(): AddrStd? {
        return if (excessesAccount != null) AddrStd(excessesAccount) else null
    }

    companion object {
        val Empty = BatteryConfigEntity(
            excessesAccount = null,
            fundReceiver = null,
            rechargeMethods = emptyList()
        )
    }
}