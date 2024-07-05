package com.tonapps.tonkeeper.ui.screen.swapnative.choose.list.holder

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.ui.screen.swapnative.choose.list.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.textPrimaryColor
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.wallet.data.core.HIDDEN_BALANCE
import uikit.extensions.drawable
import uikit.widget.FrescoView

class TokenTypeHolder(
    parent: ViewGroup,
    private val onClick: (item: Item) -> Unit
) : BaseListHolder<Item.TokenType>(parent, R.layout.view_cell_token) {

    private val iconView = findViewById<FrescoView>(R.id.icon)
    private val tokenSymbol = findViewById<AppCompatTextView>(R.id.token_symbol)
    private val tokenName = findViewById<AppCompatTextView>(R.id.token_name)
    private val balanceView = findViewById<AppCompatTextView>(R.id.balance)
    private val balanceFiatView = findViewById<AppCompatTextView>(R.id.balance_currency)
    private val tonBadge = findViewById<AppCompatTextView>(R.id.network_badge)

    override fun onBind(item: Item.TokenType) {
        itemView.setOnClickListener { onClick(item) }
        itemView.background = item.position.drawable(context)

        item.iconUri?.also { imageUrl ->
            iconView.setImageURI(imageUrl, this)
        }
        tokenSymbol.text = item.symbol
        tonBadge.visibility =
            if (listOf("jUSDC", "jUSDT", "USD₮").contains(item.symbol)) View.VISIBLE else View.GONE

        tokenName.text = item.displayName

        if (item.balance == 0.0) {
            balanceView.text = "0"
            balanceView.setTextColor(context.textSecondaryColor)
            balanceFiatView.visibility = View.INVISIBLE
        } else {
            balanceView.setTextColor(context.textPrimaryColor)
            balanceFiatView.visibility = View.VISIBLE
            if (item.hiddenBalance) {
                balanceView.text = HIDDEN_BALANCE
                balanceFiatView.text = HIDDEN_BALANCE
            } else {
                balanceView.text = item.balanceFormat
                // val fiat = item.rate * item.balance
                balanceFiatView.text = item.FiatBalance
            }

        }
    }

}