package com.tonapps.tonkeeper.fragment.jetton.list

import android.view.ViewGroup
import com.tonapps.tonkeeper.fragment.jetton.list.holder.JettonActionsHolder
import com.tonapps.tonkeeper.fragment.jetton.list.holder.JettonActionsStakedHolder
import com.tonapps.tonkeeper.fragment.jetton.list.holder.JettonDescriptionHolder
import com.tonapps.tonkeeper.fragment.jetton.list.holder.JettonDetailsHolder
import com.tonapps.tonkeeper.fragment.jetton.list.holder.JettonDividerHolder
import com.tonapps.tonkeeper.fragment.jetton.list.holder.JettonHeaderHolder
import com.tonapps.tonkeeper.fragment.jetton.list.holder.JettonLinksHolder
import com.tonapps.tonkeeper.fragment.jetton.list.holder.JettonTokenHolder
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

class JettonAdapter : BaseListAdapter() {
    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        return when (viewType) {
            JettonItem.TYPE_HEADER -> JettonHeaderHolder(parent)
            JettonItem.TYPE_ACTIONS -> JettonActionsHolder(parent)
            JettonItem.TYPE_DIVIDER -> JettonDividerHolder(parent)
            JettonItem.TYPE_ACTIONS_STAKED -> JettonActionsStakedHolder(parent)
            JettonItem.TYPE_DESCRIPTION -> JettonDescriptionHolder(parent)
            JettonItem.TYPE_DETAILS -> JettonDetailsHolder(parent)
            JettonItem.TYPE_LINKS -> JettonLinksHolder(parent)
            JettonItem.TYPE_TOKEN -> JettonTokenHolder(parent)
            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }
}