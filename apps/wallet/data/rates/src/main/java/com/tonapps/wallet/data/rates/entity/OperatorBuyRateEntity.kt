@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package com.tonapps.wallet.data.rates.entity

import android.os.Parcelable
import io.tonapi.models.OperatorBuyRate
import kotlinx.parcelize.Parcelize


@Parcelize
data class OperatorBuyRateEntity(

    val id: String,

    val name: String,

    val rate: Double,

    val currency: String,

    val logo: String,

    val minTonBuyAmount: Long? = null,

    val minTonSellAmount: Long? = null

) : Parcelable {

    constructor(
        operatorBuyRate: OperatorBuyRate
    ) : this(
        operatorBuyRate.id,
        operatorBuyRate.name,
        operatorBuyRate.rate,
        operatorBuyRate.currency,
        operatorBuyRate.logo,
        operatorBuyRate.minTonBuyAmount,
        operatorBuyRate.minTonSellAmount,
    )

}

