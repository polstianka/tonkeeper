package io.tonapi.models

import com.squareup.moshi.Json

/**
 * 
 *
 * @param jsonrpc
 * @param id
 * @param method
 * @param params
 */


data class StonfiRequest (

    @Json(name = "jsonrpc")
    val jsonrpc: String = "2.0",

    @Json(name = "id")
    val id: Int = 1,

    @Json(name = "method")
    val method: String,

    @Json(name = "params")
    val params: Any,

)