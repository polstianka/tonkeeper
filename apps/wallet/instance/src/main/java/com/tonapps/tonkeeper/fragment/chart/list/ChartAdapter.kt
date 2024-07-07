package com.tonapps.tonkeeper.fragment.chart.list

import android.view.ViewGroup
import com.tonapps.tonkeeper.api.chart.ChartPeriod
import com.tonapps.tonkeeper.fragment.chart.list.holder.ChartActionsHolder
import com.tonapps.tonkeeper.fragment.chart.list.holder.ChartActionsStakedHolder
import com.tonapps.tonkeeper.fragment.chart.list.holder.ChartDividerHolder
import com.tonapps.tonkeeper.fragment.chart.list.holder.ChartHeaderHolder
import com.tonapps.tonkeeper.fragment.chart.list.holder.ChartLineHolder
import com.tonapps.tonkeeper.fragment.chart.list.holder.ChartPeriodHolder
import com.tonapps.tonkeeper.fragment.chart.list.holder.ChartPriceHolder
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

class ChartAdapter(
    private val onPeriodSelected: (period: ChartPeriod) -> Unit,
) : BaseListAdapter() {

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        return when (viewType) {
            ChartItem.TYPE_HEADER -> ChartHeaderHolder(parent)
            ChartItem.TYPE_ACTIONS -> ChartActionsHolder(parent)
            ChartItem.TYPE_CHART -> ChartLineHolder(parent)
            ChartItem.TYPE_DIVIDER -> ChartDividerHolder(parent)
            ChartItem.TYPE_PRICE -> ChartPriceHolder(parent)
            ChartItem.TYPE_PERIOD -> ChartPeriodHolder(parent, onPeriodSelected)
            ChartItem.TYPE_ACTIONS_STAKED -> ChartActionsStakedHolder(parent)
            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }

}