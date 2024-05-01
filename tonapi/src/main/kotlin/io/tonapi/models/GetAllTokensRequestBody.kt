package io.tonapi.models


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GetAllTokensRequestBody(
    @Json(name = "jsonrpc")
    val jsonrpc: String,
    @Json(name = "id")
    val id: Int,
    @Json(name = "method")
    val method: String,
    @Json(name = "params")
    val params: Params
) {
    @JsonClass(generateAdapter = true)
    data class Params(
        @Json(name = "wallet_address")
        val walletAddress: String,
        @Json(name = "load_community")
        val loadCommunity: Boolean
    )
}