package com.tonapps.tonkeeper.fragment.jetton.list.holder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.fragment.jetton.list.JettonItem
import com.tonapps.tonkeeper.ui.screen.stake.StakedJettonViewModel
import com.tonapps.tonkeeperx.R

class JettonTabHolder(
    parent: ViewGroup,
    private val onTabClickListener: (Int) -> Unit
) : JettonHolder<JettonItem.Tabs>(parent, R.layout.view_jetton_tabs) {

    private val history = findViewById<AppCompatTextView>(R.id.history)
    private val forecast = findViewById<AppCompatTextView>(R.id.forecast)

    override fun onBind(item: JettonItem.Tabs) {
        history.setOnClickListener { onTabClickListener(StakedJettonViewModel.HISTORY_TAB_ID) }
        forecast.setOnClickListener { onTabClickListener(StakedJettonViewModel.FORECAST_TAB_ID) }
        history.isSelected = item.historySelected
        forecast.isSelected = !item.historySelected
    }
}
