package com.tonapps.tonkeeper.ui.adapter.holder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.ui.adapter.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.BaseListHolder

class HolderChartLegend(
    parent: ViewGroup
) : BaseListHolder<Item.ChartLegend>(parent, R.layout.holder_chart_legend) {
    private val text = findViewById<AppCompatTextView>(R.id.text)

    override fun onBind(item: Item.ChartLegend) {
        text.text = item.text
    }
}