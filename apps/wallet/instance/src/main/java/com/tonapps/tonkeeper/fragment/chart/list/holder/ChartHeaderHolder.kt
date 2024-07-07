package com.tonapps.tonkeeper.fragment.chart.list.holder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import com.tonapps.tonkeeper.fragment.chart.list.ChartItem
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.api.entity.TokenEntity
import uikit.extensions.cutRightBottom
import uikit.extensions.dp
import uikit.widget.FrescoView

class ChartHeaderHolder(
    parent: ViewGroup
): ChartHolder<ChartItem.Header>(parent, R.layout.view_chart_header) {

    private val balanceView = findViewById<AppCompatTextView>(R.id.send_balance)
    private val currencyView = findViewById<AppCompatTextView>(R.id.currency_balance)
    private val badgeView = findViewById<FrescoView>(R.id.badge)
    private val iconView = findViewById<FrescoView>(R.id.icon)

    override fun onBind(item: ChartItem.Header) {
        balanceView.text = item.balance
        currencyView.text = item.currencyBalance

        if (item.staked) {
            badgeView.isVisible = true
            badgeView.setImageURI(item.iconUrl, this)
            iconView.setImageURI(TokenEntity.TON.imageUri, this)
            iconView.cutRightBottom(
                radius = 14.dp.toFloat(),
                offset = 8.dp.toFloat()
            )
        } else {
            iconView.setImageURI(item.iconUrl, this)
        }
    }
}