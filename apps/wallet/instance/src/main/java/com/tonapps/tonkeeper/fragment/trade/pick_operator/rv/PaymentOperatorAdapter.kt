package com.tonapps.tonkeeper.fragment.trade.pick_operator.rv

import android.view.ViewGroup
import com.tonapps.tonkeeper.fragment.trade.pick_operator.rv.model.PaymentOperatorListItem
import com.tonapps.tonkeeper.fragment.trade.pick_operator.rv.view.PaymentOperatorHolder
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

class PaymentOperatorAdapter(
    private val onClick: (PaymentOperatorListItem) -> Unit
) : BaseListAdapter() {

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        return PaymentOperatorHolder(parent, onClick)
    }
}