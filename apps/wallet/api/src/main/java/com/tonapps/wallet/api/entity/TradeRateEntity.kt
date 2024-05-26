package com.tonapps.wallet.api.entity

import org.json.JSONObject

class TradeRateEntity(
    val id: String,
    val name: String,
    val rate: Double,
    val currency: String,
    val logo: String,
    val minTonBuyAmount: Double?,
    val minTonSellAmount: Double?,
) {
    constructor(json: JSONObject) : this(
        id = json.getString("id"),
        name = json.getString("name"),
        rate = json.getDouble("rate"),
        currency = json.getString("currency"),
        logo = json.getString("logo"),
        minTonBuyAmount = json.optDouble("min_ton_buy_amount", 0.0),
        minTonSellAmount = json.optDouble("min_ton_sell_amount", 0.0),
    )
}
