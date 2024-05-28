package io.tonapi.models

import com.squareup.moshi.Json

/**
 * @param symbol
 * @param displayName
 * @param imageUrl
 */


data class StonfiJettonInfo (

    @Json(name = "symbol")
    val symbol: kotlin.String,

    @Json(name = "display_name")
    val displayName: kotlin.String,

    @Json(name = "image_url")
    val imageUrl: kotlin.String?,

    @Json(name = "contract_address")
    val contractAddress: kotlin.String,

    @Json(name = "decimals")
    val decimals: kotlin.Int,

    @Json(name = "priority")
    val priority: kotlin.Int,

) {
    val isTon: Boolean
        get() = contractAddress == "TON" || contractAddress == "EQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAM9c"
}

