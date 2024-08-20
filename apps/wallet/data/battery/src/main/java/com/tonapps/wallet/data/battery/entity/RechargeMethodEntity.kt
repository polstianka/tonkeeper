package com.tonapps.wallet.data.battery.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RechargeMethodEntity(
    val type: RechargeMethodType,
    val rate: String,
    val symbol: String,
    val decimals: Int,
    val supportGasless: Boolean,
    val supportRecharge: Boolean,
    val image: String? = null,
    val jettonMaster: String? = null,
    val minBootstrapValue: String? = null
) : Parcelable