package com.tonapps.tonkeeper.ui.screen.swap.screens.choseToken

import android.view.ViewGroup
import com.tonapps.tonkeeper.ui.screen.swap.model.assets.Asset
import com.tonapps.tonkeeper.ui.screen.wallet.list.Item
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

class ChoseTokenAdapter(
    private val selectedToken: Asset?
) : BaseListAdapter() {

    var onClickToItem: ((token: Asset) -> Unit)? = null

    override fun createHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseListHolder<out BaseListItem> {
        return when (viewType) {
            Item.TYPE_CHOS_TOKEN -> ChoseTokenHolder(parent, onClickToItem, selectedToken)
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

}