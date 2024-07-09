package com.tonapps.tonkeeper.ui.screen.buyOrSell.screen.operator

import android.util.Log
import android.view.ViewGroup
import com.tonapps.tonkeeper.ui.screen.buyOrSell.screen.currencylist.CurrencyListHolder
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.fiatModel.LayoutByCountry
import com.tonapps.tonkeeper.ui.screen.wallet.list.Item
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

class OperatorAdapter(private val onChangeIndex: (item: Item.OperatorModel) -> Unit) :
    BaseListAdapter() {

    private var activeIndex: Int = 0

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        Log.d("statePaymentOperatorMethod", "viewType -$viewType ")
        return when (viewType) {
            Item.TYPE_SKELETON -> OperatorSkeletonHolder(
                parent = parent
            )

            Item.TYPE_OPERATOR_HOLDER -> OperatorHolder(
                parent = parent,
                getActiveIndex = { activeIndex },
                onChangeIndex = onChangeIndex,
                changeIndexHandler = {

                    val previousIndex = activeIndex
                    activeIndex = it

                    // Обновляем только измененные элементы
                    notifyItemChanged(previousIndex)
                    notifyItemChanged(activeIndex)

                }
            )

            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }
}