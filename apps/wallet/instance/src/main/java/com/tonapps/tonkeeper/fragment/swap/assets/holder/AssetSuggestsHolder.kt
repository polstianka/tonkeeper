package com.tonapps.tonkeeper.fragment.swap.assets.holder

import android.view.ViewGroup
import com.tonapps.tonkeeperx.R
import com.tonapps.tonkeeper.fragment.swap.assets.item.AssetItem
import com.tonapps.uikit.list.BaseListHolder

class AssetSuggestsHolder(
    parent: ViewGroup,
    private val clickListener: (symbol: String) -> Unit
): BaseListHolder<AssetItem.Suggests>(parent, R.layout.view_history_action) {

    override fun onBind(item: AssetItem.Suggests) {
        TODO("Not yet implemented")
    }
}