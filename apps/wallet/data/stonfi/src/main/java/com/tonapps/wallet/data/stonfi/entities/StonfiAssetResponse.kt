package com.tonapps.wallet.data.stonfi.entities

import com.squareup.moshi.Json


data class StonfiAssetResponse(
    @Json(name = "asset_list")
    val assets: List<StonfiAsset>,
)