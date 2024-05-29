package com.tonapps.tonkeeper.ui.screen.notifications.list

import android.view.ViewGroup
import com.tonapps.tonkeeper.ui.screen.notifications.list.holder.AppHolder
import com.tonapps.tonkeeper.ui.screen.notifications.list.holder.AppsHeaderHolder
import com.tonapps.tonkeeper.ui.screen.notifications.list.holder.SpaceHolder
import com.tonapps.tonkeeper.ui.screen.notifications.list.holder.WalletPushHolder
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

class Adapter: BaseListAdapter() {
    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        return when(viewType) {
            Item.TYPE_WALLET -> WalletPushHolder(parent)
            Item.TYPE_SPACE -> SpaceHolder(parent)
            Item.TYPE_APPS_HEADER -> AppsHeaderHolder(parent)
            Item.TYPE_APP -> AppHolder(parent)
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

}