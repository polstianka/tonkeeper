package com.tonapps.tonkeeper.fragment.chart.list.holder

import android.view.ViewGroup
import com.tonapps.tonkeeper.api.chart.ChartPeriod
import com.tonapps.tonkeeper.fragment.chart.list.ChartItem
import com.tonapps.tonkeeper.view.ChartView
import com.tonapps.tonkeeperx.R

class ChartLineHolder(
    parent: ViewGroup
) : ChartHolder<ChartItem.Chart>(parent, R.layout.view_chart_line) {

    private val chartView = findViewById<ChartView>(R.id.chart)

    override fun onBind(item: ChartItem.Chart) {
        chartView.setData(
            data = item.data,
            square = item.period == ChartPeriod.hour,
            labels = item.labels,
            isStaking = item.isStaking,
            minMaxPrice = item.minMaxPrice
        )
    }

}