package com.tonapps.wallet.data.stonfi.entities

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

data class StonfiAsset(
    @Json(name = "balance")
    val balance: String?,
    @Json(name = "blacklisted")
    val blacklisted: Boolean = true,
    @Json(name = "community")
    val community: Boolean = true,
    @Json(name = "contract_address")
    val contractAddress: String,
    @Json(name = "decimals")
    val decimals: Int = 9,
    @Json(name = "default_symbol")
    val defaultSymbol: Boolean,
    @Json(name = "deprecated")
    val deprecated: Boolean,
    @Json(name = "dex_price_usd")
    val dexPriceUsd: String?,
//    @Json(name = "dex_usd_price")
//    val dexUsdPrice: String?,
    @Json(name = "display_name")
    val displayName: String?,
    @Json(name = "image_url")
    val imageUrl: String?,
    @Json(name = "kind")
    val kind: StonfiAssetKind,
    @Json(name = "symbol")
    val symbol: String,
    @Json(name = "third_party_price_usd")
    val thirdPartyPriceUsd: String?,
//    @Json(name = "third_party_usd_price")
//    val thirdPartyUsdPrice: String?,
    @Json(name = "wallet_address")
    val walletAddress: String?,
) {
    @JsonClass(generateAdapter = false)
    enum class StonfiAssetKind(val value: String){
        @Json(name = "Jetton") Jetton("Jetton"),
        @Json(name = "Wton") Wton("Wton"),
        @Json(name = "Ton") Ton("Ton");
    }
}
