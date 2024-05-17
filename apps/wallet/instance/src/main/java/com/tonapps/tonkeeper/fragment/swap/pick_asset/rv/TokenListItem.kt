package com.tonapps.tonkeeper.fragment.swap.pick_asset.rv

import androidx.annotation.AttrRes
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAsset
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell

data class TokenListItem(
    val model: DexAsset,
    val iconUrl: String,
    val symbol: String,
    val amountCrypto: String,
    val name: String,
    val amountFiat: String,
    @AttrRes val amountCryptoColor: Int,
    val position: ListCell.Position
) : BaseListItem(1)
