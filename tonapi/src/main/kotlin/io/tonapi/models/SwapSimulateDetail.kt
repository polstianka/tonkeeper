@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package io.tonapi.models


import com.squareup.moshi.Json
import java.math.BigInteger


data class SwapSimulateDetail(

    @Json(name = "ask_address")
    val askAddress: String,

    @Json(name = "ask_units")
    val askUnits: BigInteger,

    @Json(name = "fee_address")
    val feeAddress: String,

    @Json(name = "fee_percent")
    val feePercent: String,

    @Json(name = "fee_units")
    val feeUnits: BigInteger,

    @Json(name = "min_ask_units")
    val minAskUnits: BigInteger,

    @Json(name = "offer_address")
    val offerAddress: String,

    @Json(name = "offer_units")
    val offerUnits: BigInteger,

    @Json(name = "pool_address")
    val poolAddress: String,

    @Json(name = "price_impact")
    val priceImpact: String,

    @Json(name = "router_address")
    val routerAddress: String,

    @Json(name = "slippage_tolerance")
    val slippageTolerance: String,

    @Json(name = "swap_rate")
    val swapRate: String,

)