package com.tonapps.tonkeeper.fragment.jetton.list.holder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.extensions.rateSpannable
import com.tonapps.tonkeeper.fragment.jetton.list.JettonItem
import com.tonapps.tonkeeperx.R
import uikit.widget.FrescoView

class JettonHeaderHolder(
    parent: ViewGroup
) : JettonHolder<JettonItem.Header>(parent, R.layout.view_jetton_header) {

    private val iconView = findViewById<FrescoView>(R.id.icon)
    private val balanceView = findViewById<AppCompatTextView>(R.id.send_balance)
    private val currencyView = findViewById<AppCompatTextView>(R.id.currency_balance)
    private val rateView = findViewById<AppCompatTextView>(R.id.rate)

    override fun onBind(item: JettonItem.Header) {
        iconView.setImageURI(item.iconUrl)
        balanceView.text = item.balance
        currencyView.text = item.currencyBalance
        rateView.text = context.rateSpannable(item.rate, item.diff24h)
    }
}