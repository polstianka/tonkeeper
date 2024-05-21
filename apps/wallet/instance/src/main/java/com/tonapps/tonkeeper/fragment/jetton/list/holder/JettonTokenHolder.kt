package com.tonapps.tonkeeper.fragment.jetton.list.holder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.core.view.updateMargins
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.extensions.buildRateString
import com.tonapps.tonkeeper.fragment.jetton.list.JettonItem
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.uikit.list.ListCell
import uikit.extensions.dp
import uikit.extensions.drawable
import uikit.widget.FrescoView

class JettonTokenHolder(
    parent: ViewGroup
) : JettonHolder<JettonItem.Token>(parent, R.layout.view_cell_jetton) {

    private val iconView = findViewById<FrescoView>(R.id.icon)
    private val titleView = findViewById<AppCompatTextView>(R.id.title)
    private val rateView = findViewById<AppCompatTextView>(R.id.rate)
    private val balanceView = findViewById<AppCompatTextView>(R.id.send_balance)
    private val balanceFiatView = findViewById<AppCompatTextView>(R.id.balance_currency)

    override fun onBind(item: JettonItem.Token) {
        (itemView.layoutParams as RecyclerView.LayoutParams).updateMargins(
            left = 16.dp,
            right = 16.dp
        )
        itemView.background = ListCell.Position.SINGLE.drawable(context)
        iconView.setImageURI(item.iconUri, this)
        titleView.text = item.symbol
        balanceView.text = item.balanceFormat
        balanceFiatView.isVisible = true
        balanceFiatView.text = item.fiatFormat
        setRate(item.rate, item.rateDiff24h)
    }

    private fun setRate(rate: CharSequence, rateDiff24h: String) {
        rateView.isVisible = true
        rateView.text = context.buildRateString(rate, rateDiff24h)
        rateView.setTextColor(context.textSecondaryColor)
    }
}
