package com.tonapps.tonkeeper.ui.adapter.holder.staking

import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import com.tonapps.tonkeeper.api.chart.ChartPeriod
import com.tonapps.tonkeeper.ui.adapter.Item
import com.tonapps.tonkeeper.view.ChartPeriodView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.BaseListHolder

class HolderStakingPageChartPeriod(
    parent: ViewGroup,
) : BaseListHolder<Item.StakingPageChartPeriod>(parent, R.layout.view_chart_period) {

    private val periodView = findViewById<ChartPeriodView>(R.id.period)

    init {
        for (i in 0 until periodView.childCount) {
            val child = periodView.getChildAt(i)
            if (child is TextView) {
                val text = child.text
                if (text == ChartPeriod.hour.value
                    || text == ChartPeriod.day.value
                    || text == ChartPeriod.week.value
                ) {
                    child.isVisible = false
                }
            }
        }
    }

    override fun onBind(item: Item.StakingPageChartPeriod) {
        periodView.doOnPeriodSelected = item.listener
        periodView.selectedPeriod = item.period
    }
}