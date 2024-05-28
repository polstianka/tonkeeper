package io.tonapi.models

import com.squareup.moshi.Json

/**
 * 
 *
 * @param askAddress
 * @param offerAddress
 * @param askUnits
 * @param referralAddress
 * @param slippageTolerance
 */

data class StonfiSimulateReversedParamsRequest (

    @Json(name = "ask_address")
    val askAddress: String,

    @Json(name = "offer_address")
    val offerAddress: String,

    @Json(name = "ask_units")
    val askUnits: String,

    @Json(name = "referral_address")
    val referralAddress: String,

    @Json(name = "slippage_tolerance")
    val slippageTolerance: String,

)