package com.tonapps.wallet.data.battery.entity

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.ton.block.AddrStd

@Parcelize
data class BatteryConfigEntity(
    val excessesAccount: String?,
    val fundReceiver: String?,
    val rechargeMethods: List<RechargeMethodEntity>,
) : Parcelable {

    @IgnoredOnParcel
    val excessesAddress: AddrStd? by lazy {
        excessesAccount?.let { AddrStd(it) }
    }

    companion object {
        val Empty = BatteryConfigEntity(
            excessesAccount = null,
            fundReceiver = null,
            rechargeMethods = emptyList()
        )
    }
}