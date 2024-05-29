package com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.fiatModel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Item(
    @SerialName("action_button")val action_button: ActionButton,
    @SerialName("assets")val assets: List<String>? = null,
    @SerialName("badge")val badge: String? = null,
    @SerialName("description")val description: String,
    @SerialName("disabled")val disabled: Boolean,
    @SerialName("icon_url")val icon_url: String,
    @SerialName("id")val id: String,
    @SerialName("info_buttons")val info_buttons: List<InfoButton>?,
    @SerialName("subtitle")val subtitle: String,
    @SerialName("successUrlPattern")val successUrlPattern: SuccessUrlPattern? = null,
    @SerialName("title")val title: String
)