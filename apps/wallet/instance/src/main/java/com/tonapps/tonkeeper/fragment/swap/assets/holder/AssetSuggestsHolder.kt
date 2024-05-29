package com.tonapps.tonkeeper.fragment.swap.assets.holder

import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.tonapps.tonkeeperx.R
import com.tonapps.tonkeeper.fragment.swap.assets.item.AssetItem
import com.tonapps.tonkeeper.ui.component.swap.TokenButtonView
import com.tonapps.uikit.list.BaseListHolder

class AssetSuggestsHolder(
    parent: ViewGroup,
    private val clickListener: (symbol: String) -> Unit
): BaseListHolder<AssetItem.Suggests>(parent, R.layout.view_cell_suggest) {

    private val token1 = findViewById<TokenButtonView>(R.id.token1)
    private val token2 = findViewById<TokenButtonView>(R.id.token2)

    override fun onBind(item: AssetItem.Suggests) {
        val suggest1 = item.assets.getOrNull(0)
        val suggest2 = item.assets.getOrNull(1)
        if (suggest1 != null) {
            token1.setToken(suggest1.imageUrl?.toUri(), suggest1.symbol)
            token1.isVisible = true
            token1.setOnClickListener {
                clickListener.invoke(suggest1.symbol)
            }
        } else {
            token1.isVisible = false
        }
        if (suggest2 != null) {
            token2.setToken(suggest2.imageUrl?.toUri(), suggest2.symbol)
            token2.isVisible = true
            token2.setOnClickListener {
                clickListener.invoke(suggest2.symbol)
            }
        } else {
            token2.isVisible = false
        }
    }
}