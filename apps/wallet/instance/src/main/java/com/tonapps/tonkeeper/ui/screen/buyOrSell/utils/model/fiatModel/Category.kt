package com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.fiatModel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Category(
    @SerialName("assets")val assets: List<String>,
    @SerialName("items")val items: List<ItemX>,
    @SerialName("subtitle")val subtitle: String,
    @SerialName("title")val title: String,
    @SerialName("type")val type: String
)