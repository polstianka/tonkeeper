package com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.fiatModel

import kotlinx.serialization.Serializable

@Serializable
data class SuccessUrlPattern(
    val pattern: String? = null,
    val purchaseIdIndex: Int? = null
)