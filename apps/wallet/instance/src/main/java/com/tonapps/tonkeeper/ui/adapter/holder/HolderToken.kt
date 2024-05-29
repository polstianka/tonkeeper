package com.tonapps.tonkeeper.ui.adapter.holder

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import com.tonapps.tonkeeper.extensions.buildRateString
import com.tonapps.tonkeeper.fragment.jetton.JettonScreen
import com.tonapps.tonkeeper.ui.adapter.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.accentOrangeColor
import com.tonapps.uikit.color.textPrimaryColor
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.core.HIDDEN_BALANCE
import com.tonapps.wallet.localization.Localization
import uikit.extensions.drawable
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.FrescoView

class HolderToken(parent: ViewGroup) :
    BaseListHolder<Item.Token>(parent, R.layout.holder_jetton_optimized) {
    private val iconView = findViewById<FrescoView>(R.id.icon)
    private val titleView = findViewById<AppCompatTextView>(R.id.title)
    private val rateView = findViewById<AppCompatTextView>(R.id.rate)
    private val balanceView = findViewById<AppCompatTextView>(R.id.balance)
    private val balanceFiatView = findViewById<AppCompatTextView>(R.id.balance_currency)
    private val labelView = findViewById<AppCompatTextView>(R.id.label)

    override fun onBind(item: Item.Token) {
        itemView.background = item.position.drawable(context)
        itemView.setOnClickListener {
            if (item.onClickListener != null) {
                item.onClickListener.invoke()
            } else {
                context.navigation?.add(
                    JettonScreen.newInstance(
                        item.address,
                        item.name,
                        item.symbol
                    )
                )
            }
        }
        iconView.setImageURI(item.iconUri, this)
        labelView.isVisible = item.address == TokenEntity.TETHER_USDT_ADDRESS
        titleView.text = item.symbol
        balanceView.text = if (item.hiddenBalance) {
            HIDDEN_BALANCE
        } else {
            item.balanceFormat
        }


        val isEmptyTokenInSwapMode =
            (item.mode == Item.TokenDisplayMode.SwapSelector && item.balanceFormat == "0")

        if (isEmptyTokenInSwapMode) {
            balanceView.setTextColor(context.textSecondaryColor)
        } else {
            balanceView.setTextColor(context.textPrimaryColor)
        }

        if (item.testnet || isEmptyTokenInSwapMode) {
            balanceFiatView.visibility = View.GONE
        } else {
            balanceFiatView.visibility = View.VISIBLE
            if (item.hiddenBalance) {
                balanceFiatView.text = HIDDEN_BALANCE
            } else {
                balanceFiatView.text = item.fiatFormat
            }
        }

        if (item.mode == Item.TokenDisplayMode.SwapSelector) {
            rateView.visibility = View.VISIBLE
            rateView.setTextColor(context.textSecondaryColor)
            rateView.text = item.name
        } else if (item.testnet) {
            rateView.visibility = View.GONE
        } else {
            setRate(item.rate, item.rateDiff24h, item.verified)
        }
    }

    private fun setRate(rate: CharSequence, rateDiff24h: String, verified: Boolean) {
        rateView.visibility = View.VISIBLE
        if (verified) {
            rateView.text = context.buildRateString(rate, rateDiff24h)
            rateView.setTextColor(context.textSecondaryColor)
        } else {
            rateView.setText(Localization.unverified_token)
            rateView.setTextColor(context.accentOrangeColor)
        }
    }
}