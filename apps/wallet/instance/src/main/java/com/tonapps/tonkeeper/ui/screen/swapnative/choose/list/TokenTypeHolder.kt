package com.tonapps.tonkeeper.ui.screen.swapnative.choose.list

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.textPrimaryColor
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.wallet.data.core.HIDDEN_BALANCE
import uikit.extensions.drawable
import uikit.widget.FrescoView

class TokenTypeHolder(
    parent: ViewGroup,
    private val onClick: (item: TokenTypeItem) -> Unit
) : BaseListHolder<TokenTypeItem>(parent, R.layout.view_cell_token) {

    private val iconView = findViewById<FrescoView>(R.id.icon)
    private val tokenSymbol = findViewById<AppCompatTextView>(R.id.token_symbol)
    private val tokenName = findViewById<AppCompatTextView>(R.id.token_name)
    private val balanceView = findViewById<AppCompatTextView>(R.id.balance)
    private val balanceFiatView = findViewById<AppCompatTextView>(R.id.balance_currency)

    override fun onBind(item: TokenTypeItem) {
        itemView.setOnClickListener { onClick(item) }
        itemView.background = item.position.drawable(context)
//        itemView.setOnClickListener {
//            context.navigation?.add(JettonScreen.newInstance(item.address, item.name, item.symbol))
//        }

        // token icon background color
        item.iconUri?.also { imageUrl ->
            iconView.setImageURI(imageUrl, this)
        }
        tokenSymbol.text = item.symbol
        tokenName.text = item.displayName

        if (item.balance == 0.0) {
            balanceView.text = "0"
            balanceView.setTextColor(context.textSecondaryColor)
        } else {
            balanceView.setTextColor(context.textPrimaryColor)
            if (item.hiddenBalance) {
                balanceView.text = HIDDEN_BALANCE
            } else {
                balanceView.text = item.balanceFormat
            }
        }

        /*balanceView.text = if (item.hiddenBalance) {
            HIDDEN_BALANCE
        } else {
            item.balanceFormat
        }*/

        /*if (item.testnet) {
            balanceFiatView.visibility = View.GONE
        } else {
            balanceFiatView.visibility = View.VISIBLE
            if (item.hiddenBalance) {
                balanceFiatView.text = HIDDEN_BALANCE
            } else {
                balanceFiatView.text = item.fiatFormat
            }
            // setRate(item.rate, item.rateDiff24h, item.verified)
        }*/
    }

    /*private fun setRate(rate: CharSequence, rateDiff24h: String, verified: Boolean) {
        rateView.visibility = View.VISIBLE
        if (verified) {
            rateView.text = context.buildRateString(rate, rateDiff24h)
            rateView.setTextColor(context.textSecondaryColor)
        } else {
            rateView.setText(Localization.unverified_token)
            rateView.setTextColor(context.accentOrangeColor)
        }
    }*/

}