package io.tonapi.models

import com.squareup.moshi.Json

/**
 * @param result
 */


data class StonfiSwapResponse (

    @Json(name = "result")
    val result: StonfiSwapResultResponse,

)