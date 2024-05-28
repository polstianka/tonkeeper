package io.tonapi.models

import com.squareup.moshi.Json

/**
 * 
 *
 * @param askAddress
 * @param offerAddress
 * @param offerUnits
 * @param referralAddress
 * @param slippageTolerance
 */

data class StonfiSimulateParamsRequest (

    @Json(name = "ask_address")
    val askAddress: String,

    @Json(name = "offer_address")
    val offerAddress: String,

    @Json(name = "offer_units")
    val offerUnits: String,

    @Json(name = "referral_address")
    val referralAddress: String,

    @Json(name = "slippage_tolerance")
    val slippageTolerance: String,

)