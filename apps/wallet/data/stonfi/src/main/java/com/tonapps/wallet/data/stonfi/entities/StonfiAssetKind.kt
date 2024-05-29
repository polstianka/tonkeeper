package com.tonapps.wallet.data.stonfi.entities

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
enum class StonfiAssetKind {
    @Json(name = "Jetton") Jetton,
    @Json(name = "Wton") Wton,
    @Json(name = "Ton") Ton;
}