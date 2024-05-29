package com.tonapps.tonkeeper.api.swap

import com.tonapps.uikit.list.BaseListItem
import org.json.JSONObject

data class StonfiSwapAsset(
    val contractAddress: String,
    val symbol: String,
    val displayName: String,
    val imageURL: String,
    val decimals: Int,
    val kind: String,
    val deprecated: Boolean,
    val community: Boolean,
    val blacklisted: Boolean,
    val defaultSymbol: Boolean,
    val balance: String,
    var balanceInCurrency: String = ""
): BaseListItem() {
    constructor(json: JSONObject) : this(
        json.optString("contract_address", ""),
        json.optString("symbol", ""),
        json.optString("display_name", ""),
        json.optString("image_url", ""),
        json.optInt("decimals", 9),
        json.optString("kind", "Jetton"),
        json.optBoolean("deprecated", false),
        json.optBoolean("community", false),
        json.optBoolean("blacklisted", false),
        json.optBoolean("default_symbol", false),
        json.optString("balance", "0"),
    )

    override fun toString(): String {
        return "StonfiSwapAsset(symbol='$symbol', displayName='$displayName')"
    }
}

data class SwapSimulateData(
    val offerAddresses: String,
    val askAddresses: String,
    val routeAddress: String?,
    val poolAddress: String,
    val offerUnits: String,
    val askUnits: String,
    val slippageTolerance: String,
    val minAskUnits: String,
    val swapRate: String,
    val priceImpact: String,
    val feeAddress: String,
    val feeUnits: String,
    val feePercent: String
) {
    constructor(json: JSONObject) : this(
        json.optString("offer_address", ""),
        json.optString("ask_address", ""),
        json.optString("route_address", ""),
        json.optString("pool_address", ""),
        json.optString("offer_units", "0"),
        json.optString("ask_units", "0"),
        json.optString("slippage_tolerance", "0.001"),
        json.optString("min_ask_units", "0"),
        json.optString("swap_rate", "0"),
        json.optString("price_impact", "0"),
        json.optString("fee_address", ""),
        json.optString("fee_units", "1"),
        json.optString("fee_percent", "0"),
    )
}