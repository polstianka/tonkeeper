package com.tonapps.tonkeeper.ui.screen.wallet.main.list.holder

import android.view.ViewGroup
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.Item
import com.tonapps.tonkeeperx.R
import uikit.extensions.dp

class SpaceHolder(parent: ViewGroup): Holder<Item.Space>(parent,  R.layout.view_wallet_space) {
    override fun onBind(item: Item.Space) {
        itemView.minimumHeight = if (item.large) 20.dp else 16.dp

    }
}