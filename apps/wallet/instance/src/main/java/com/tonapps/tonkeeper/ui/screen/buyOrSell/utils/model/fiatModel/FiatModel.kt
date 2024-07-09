package com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.fiatModel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FiatModel(
    @SerialName("data")val data: Data,
    val success: Boolean
)