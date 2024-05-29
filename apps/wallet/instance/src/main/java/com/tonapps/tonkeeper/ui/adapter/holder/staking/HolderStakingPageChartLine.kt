package com.tonapps.tonkeeper.ui.adapter.holder.staking

import android.view.ViewGroup
import com.tonapps.tonkeeper.api.chart.ChartPeriod
import com.tonapps.tonkeeper.ui.adapter.Item
import com.tonapps.tonkeeper.view.ChartView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.accentGreenColor
import com.tonapps.uikit.list.BaseListHolder

class HolderStakingPageChartLine(
    parent: ViewGroup
) : BaseListHolder<Item.StakingPageChart>(parent, R.layout.view_chart_line) {

    private val chartView = findViewById<ChartView>(R.id.chart)

    override fun onBind(item: Item.StakingPageChart) {
        chartView.setData(item.data, item.period == ChartPeriod.hour)
        chartView.setColor(context.accentGreenColor)
    }

}