package com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.RatesModel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemRates(
    @SerialName("id")val id: String,
    @SerialName("currency") val currency: String,
    @SerialName("logo")val logo: String,
    @SerialName("min_ton_buy_amount") val min_ton_buy_amount: Int? = null,
    @SerialName("min_ton_sell_amount")val min_ton_sell_amount: Long? = null,
    @SerialName("name")val name: String ,
    @SerialName("rate") val rate: Double
)