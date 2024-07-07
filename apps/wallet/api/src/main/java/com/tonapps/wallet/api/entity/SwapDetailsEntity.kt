package com.tonapps.wallet.api.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SwapDetailsEntity(
    val offerUnits: String,
    val askUnits: String,
    val priceImpact: String,
    val minReceived: String,
    val routerAddress: String,
    val poolAddress: String,
    val providerFeeUnits: String,
    val feeAddress: String,
    val swapRate: String
) : Parcelable