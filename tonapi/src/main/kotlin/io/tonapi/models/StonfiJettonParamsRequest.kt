package io.tonapi.models

import com.squareup.moshi.Json

/**
 * 
 *
 * @param loadCommunity
 */

data class StonfiJettonParamsRequest (

    @Json(name = "load_community")
    val loadCommunity: Boolean

)