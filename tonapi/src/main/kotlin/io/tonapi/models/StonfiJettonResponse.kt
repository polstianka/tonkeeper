package io.tonapi.models

import com.squareup.moshi.Json

/**
 * @param result
 */


data class StonfiJettonResponse (

    @Json(name = "result")
    val result: StonfiJettonResultResponse,

)