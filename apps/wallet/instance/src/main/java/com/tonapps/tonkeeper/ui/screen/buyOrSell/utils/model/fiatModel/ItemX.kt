package com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.fiatModel

import kotlinx.serialization.Serializable

@Serializable
data class ItemX(
    val action_button: ActionButton,
    val badge: String? = null,
    val description: String,
    val disabled: Boolean,
    val icon_url: String,
    val id: String,
    val subtitle: String,
    val title: String
)