package com.tonapps.wallet.data.swap

import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.entity.TokenEntity

data class AssetModel(
    val token: TokenEntity,
    val balance: Float,
    val walletAddress: String,
    val fiatBalance: Float,
    val isTon: Boolean,
    override val position: ListCell.Position
) : BaseListItem(), ListCell