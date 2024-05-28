package io.tonapi.models

import com.squareup.moshi.Json

/**
 *
 *
 * @param offerAddress
 * @param askAddress
 * @param routerAddress
 * @param poolAddress
 * @param offerUnits
 * @param askUnits
 * @param slippageTolerance
 * @param minAskUnits
 * @param swapRate
 * @param priceImpact
 * @param feeAddress
 * @param feeUnits
 * @param feePercent
 */


data class StonfiSwapResultResponse (

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
    val feePercent: String,

)