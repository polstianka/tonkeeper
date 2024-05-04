package io.tonapi.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JettonAddressResponse(
    @Json(name = "address")
    val address: String
)