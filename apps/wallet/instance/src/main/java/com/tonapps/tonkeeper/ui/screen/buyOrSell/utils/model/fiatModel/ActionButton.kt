package com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.fiatModel

import kotlinx.serialization.Serializable

@Serializable
data class ActionButton(
    val title: String,
    val url: String
)