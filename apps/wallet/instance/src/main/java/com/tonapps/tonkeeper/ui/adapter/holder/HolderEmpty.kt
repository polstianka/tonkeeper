package com.tonapps.tonkeeper.ui.adapter.holder

import android.view.View
import android.view.ViewGroup
import com.tonapps.tonkeeper.ui.adapter.Item
import com.tonapps.uikit.list.BaseListHolder

class HolderEmpty(
    parent: ViewGroup
) : BaseListHolder<Item>(View(parent.context)) {
    override fun onBind(item: Item) {

    }
}