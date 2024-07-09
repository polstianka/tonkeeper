package com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.RatesModel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RatesModel(
    @SerialName("items")val itemRates: List<ItemRates>? = null
)