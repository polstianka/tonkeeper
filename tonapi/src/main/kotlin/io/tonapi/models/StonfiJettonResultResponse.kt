package io.tonapi.models

import com.squareup.moshi.Json

/**
 * @param assets
 */


data class StonfiJettonResultResponse (

    @Json(name = "assets")
    val assets: List<StonfiJettonInfo>,

)