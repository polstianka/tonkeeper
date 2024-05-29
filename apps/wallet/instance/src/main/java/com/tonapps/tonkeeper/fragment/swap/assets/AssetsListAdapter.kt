package com.tonapps.tonkeeper.fragment.swap.assets

import android.view.ViewGroup
import com.tonapps.tonkeeper.fragment.swap.assets.holder.AssetItemHolder
import com.tonapps.tonkeeper.fragment.swap.assets.holder.AssetLabelHolder
import com.tonapps.tonkeeper.fragment.swap.assets.holder.AssetSuggestsHolder
import com.tonapps.tonkeeper.fragment.swap.assets.item.AssetItem

import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

class AssetsListAdapter(private val onClickListener: (symbol: String) -> Unit): BaseListAdapter() {
    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        return when (viewType) {
            AssetItem.TYPE_ITEM -> AssetItemHolder(parent, onClickListener)
            AssetItem.TYPE_LABEL -> AssetLabelHolder(parent)
            AssetItem.TYPE_SUGGESTS -> AssetSuggestsHolder(parent, onClickListener)
            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }
}