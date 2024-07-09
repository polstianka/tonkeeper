package com.tonapps.tonkeeper.ui.screen.swap.model.assets

import kotlinx.serialization.Serializable

@Serializable
data class AssetsModel(
    val asset_list: List<Asset>?
)