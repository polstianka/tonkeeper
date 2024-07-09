package com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.fiatModel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemXX(
    @SerialName("action_button")val action_button: ActionButton,
    @SerialName("assets")val assets: List<String>? = null,
    @SerialName("description")val description: String,
    @SerialName("disabled")val disabled: Boolean,
    @SerialName("icon_url")val icon_url: String,
    @SerialName("id")val id: String,
    @SerialName("subtitle")val subtitle: String,
    @SerialName("title")val title: String
)