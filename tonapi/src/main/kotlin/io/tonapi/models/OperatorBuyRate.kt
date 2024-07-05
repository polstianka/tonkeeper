@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package io.tonapi.models

import com.squareup.moshi.Json


data class OperatorBuyRate(

    val id: String,

    val name: String,

    val rate: Double,

    val currency: String,

    val logo: String,

    @Json(name = "min_ton_buy_amount")
    val minTonBuyAmount: Long? = null,

    @Json(name = "min_ton_sell_amount")
    val minTonSellAmount: Long? = null

)

