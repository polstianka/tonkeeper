package com.tonapps.tonkeeper.ui.screen.buyOrSell.view

import android.util.Log
import android.view.ViewGroup
import com.tonapps.tonkeeper.ui.screen.buyOrSell.screen.operator.OperatorHolder
import com.tonapps.tonkeeper.ui.screen.buyOrSell.screen.operator.OperatorSkeletonHolder
import com.tonapps.tonkeeper.ui.screen.wallet.list.Item
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

class ListPayMethodAdapter :
    BaseListAdapter() {

    private var activeIndex: Int = 0

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        Log.d("statePaymentOperatorMethod", "viewType -$viewType ")
        return when (viewType) {
            Item.TYPE_LIST_PAY_METHOD -> ListPayMethodHolder(
                parent,
                getActiveIndex = { activeIndex },
                changeIndexHandler = {
                    val previousIndex = activeIndex
                    activeIndex = it

                    // Обновляем только измененные элементы
                    notifyItemChanged(previousIndex)
                    notifyItemChanged(activeIndex)
                })

            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }
}