package com.tonapps.tonkeeper.fragment.chart.list.holder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.fragment.chart.list.ChartItem
import com.tonapps.tonkeeperx.R

class ChartHeaderHolder(
    parent: ViewGroup
): ChartHolder<ChartItem.Header>(parent, R.layout.view_chart_header) {

    private val balanceView = findViewById<AppCompatTextView>(R.id.send_balance)
    private val currencyView = findViewById<AppCompatTextView>(R.id.currency_balance)

    override fun onBind(item: ChartItem.Header) {
        balanceView.text = item.balance
        currencyView.text = item.currencyBalance
    }
}