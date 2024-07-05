

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package io.tonapi.models

import io.tonapi.models.AccountAddress
import io.tonapi.models.JettonPreview
import io.tonapi.models.TokenRates

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass




data class Asset (

    /*@Json(name = "balance")
    val balance: kotlin.String,*/

    @Json(name = "blacklisted")
    val blacklisted: Boolean,

    @Json(name = "community")
    val community: Boolean,

    @Json(name = "contract_address")
    val contractAddress: kotlin.String,

    @Json(name = "decimals")
    val decimals: Int,

//    @Json(name = "default_symbol")
//    val defaultSymbol: Boolean,

    @Json(name = "deprecated")
    val deprecated: Boolean,

//    @Json(name = "dex_price_usd")
//    val dexPriceUsd: String,
//
//    @Json(name = "dex_usd_price")
//    val dexUsdPrice: String,

    @Json(name = "display_name")
    val displayName: String?,

    @Json(name = "image_url")
    val imageUrl: String?,

    @Json(name = "symbol")
    val symbol: String,

//    @Json(name = "wallet_address")
//    val walletAddress: kotlin.String,


)

