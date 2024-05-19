package com.tonapps.tonkeeper.ui.screen.token.list.holder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.ui.screen.token.list.Item
import com.tonapps.tonkeeperx.R
import uikit.widget.FrescoView

class BalanceHolder(parent: ViewGroup): Holder<Item.Balance>(parent, R.layout.view_token_balance) {

    private val balanceView = findViewById<AppCompatTextView>(R.id.balance)
    private val fiatBalanceView = findViewById<AppCompatTextView>(R.id.fiat_balance)
    private val iconView = findViewById<FrescoView>(R.id.icon)

    override fun onBind(item: Item.Balance) {
        balanceView.text = item.balance
        fiatBalanceView.text = item.fiat
        iconView.setImageURI(item.iconUri)
    }
}