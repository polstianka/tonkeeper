package com.tonapps.tonkeeper.fragment.jetton.list.holder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import com.tonapps.tonkeeper.extensions.rateSpannable
import com.tonapps.tonkeeper.fragment.jetton.list.JettonItem
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.api.entity.TokenEntity
import uikit.extensions.cutRightBottom
import uikit.extensions.dp
import uikit.widget.FrescoView

class JettonHeaderHolder(
    parent: ViewGroup
) : JettonHolder<JettonItem.Header>(parent, R.layout.view_jetton_header) {

    private val iconView = findViewById<FrescoView>(R.id.icon)
    private val balanceView = findViewById<AppCompatTextView>(R.id.send_balance)
    private val currencyView = findViewById<AppCompatTextView>(R.id.currency_balance)
    private val rateView = findViewById<AppCompatTextView>(R.id.rate)
    private val badgeView = findViewById<FrescoView>(R.id.badge)

    override fun onBind(item: JettonItem.Header) {
        balanceView.text = item.balance
        currencyView.text = item.currencyBalance
        if (item.rate != null && item.diff24h != null) {
            rateView.text = context.rateSpannable(item.rate, item.diff24h)
        }

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