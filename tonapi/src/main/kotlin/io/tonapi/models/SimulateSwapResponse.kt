package io.tonapi.models


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SimulateSwapResponse(
    @Json(name = "offer_address")
    val offerAddress: String,
    @Json(name = "ask_address")
    val askAddress: String,
    @Json(name = "router_address")
    val routerAddress: String,
    @Json(name = "pool_address")
    val poolAddress: String,
    @Json(name = "offer_units")
    val offerUnits: String,
    @Json(name = "ask_units")
    val askUnits: String,
    @Json(name = "slippage_tolerance")
    val slippageTolerance: String,
    @Json(name = "min_ask_units")
    val minAskUnits: String,
    @Json(name = "swap_rate")
    val swapRate: String,
    @Json(name = "price_impact")
    val priceImpact: String,
    @Json(name = "fee_address")
    val feeAddress: String,
    @Json(name = "fee_units")
    val feeUnits: String,
    @Json(name = "fee_percent")
    val feePercent: String
)