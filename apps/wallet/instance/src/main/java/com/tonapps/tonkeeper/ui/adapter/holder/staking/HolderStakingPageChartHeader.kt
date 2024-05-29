package com.tonapps.tonkeeper.ui.adapter.holder.staking

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.ui.adapter.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.BaseListHolder

class HolderStakingPageChartHeader(
    parent: ViewGroup
) : BaseListHolder<Item.StakingPageChartHeader>(parent, R.layout.holder_staking_page_chart_header) {

    private val priceView = findViewById<AppCompatTextView>(R.id.price)

    override fun onBind(item: Item.StakingPageChartHeader) {
        priceView.text = item.apy
    }
}
