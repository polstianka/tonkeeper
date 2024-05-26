package com.tonapps.tonkeeper.fragment.swap.pick_asset.rv

import android.net.Uri
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAssetBalance
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell

data class TokenListItem(
    val model: DexAssetBalance,
    val iconUri: Uri,
    val symbol: String,
    val name: String,
    val position: ListCell.Position
) : BaseListItem(1)