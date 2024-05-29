package com.tonapps.tonkeeper.fragment.swap.assets.item

import android.net.Uri
import androidx.annotation.StringRes
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.stonfi.entities.StonfiAsset

sealed class AssetItem(
    type: Int,
): BaseListItem(type) {
    companion object {
        const val TYPE_ITEM = 1
        const val TYPE_LABEL = 2
        const val TYPE_SUGGESTS = 3
    }

    data class Item(
        var position: ListCell.Position,
        val icon: Uri?,
        val symbol: String,
        val subtitle: String,
        val balanceFormat: CharSequence,
        val balanceFiatFormat: CharSequence,
        val address: CharSequence,
        val byTon: Boolean,
    ): AssetItem(TYPE_ITEM)

    data class Label(
        @StringRes val labelRes: Int
    ): AssetItem(TYPE_LABEL)

    data class Suggests(
        val assets: List<StonfiAsset>
    ): AssetItem(TYPE_SUGGESTS)
}