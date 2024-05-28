

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package io.tonapi.models

import io.tonapi.models.JettonBalance

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass



data class AssetList (

    @Json(name = "asset_list")
    val assetList: kotlin.collections.List<Asset>

)

