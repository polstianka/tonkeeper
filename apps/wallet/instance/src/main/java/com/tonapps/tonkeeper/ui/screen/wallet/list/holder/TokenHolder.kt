package com.tonapps.tonkeeper.ui.screen.wallet.list.holder

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import com.tonapps.tonkeeper.extensions.buildRateString
import com.tonapps.tonkeeper.fragment.jetton.JettonScreen
import com.tonapps.tonkeeper.ui.screen.stake.StakedJettonArgs
import com.tonapps.tonkeeper.ui.screen.stake.StakedJettonScreen
import com.tonapps.tonkeeper.ui.screen.wallet.list.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.accentOrangeColor
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.core.HIDDEN_BALANCE
import com.tonapps.wallet.localization.Localization
import uikit.extensions.cutRightBottom
import uikit.extensions.dp
import uikit.extensions.drawable
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.FrescoView

class TokenHolder(parent: ViewGroup) : Holder<Item.Token>(parent, R.layout.view_cell_jetton) {

    private val iconView = findViewById<FrescoView>(R.id.icon)
    private val titleView = findViewById<AppCompatTextView>(R.id.title)
    private val rateView = findViewById<AppCompatTextView>(R.id.rate)
    private val balanceView = findViewById<AppCompatTextView>(R.id.send_balance)
    private val balanceFiatView = findViewById<AppCompatTextView>(R.id.balance_currency)
    private val badgeView = findViewById<FrescoView>(R.id.badge)

    override fun onBind(item: Item.Token) {
        itemView.background = item.position.drawable(context)
        itemView.setOnClickListener {
            if (item.staked) {
                context.navigation?.add(
                    StakedJettonScreen.newInstance(
                        StakedJettonArgs(
                            item.address,
                            item.rate.toString()
                        )
                    )
                )
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
        if (item.staked) {
            badgeView.isVisible = true
            badgeView.setImageURI(item.iconUri, this)
            iconView.setImageURI(TokenEntity.TON.imageUri, this)
            iconView.cutRightBottom(
                radius = 11.dp.toFloat(),
                offset = 5.dp.toFloat()
            )
        } else {
            iconView.setImageURI(item.iconUri, this)
        }
        titleView.text = item.symbol
        balanceView.text = if (item.hiddenBalance) {
            HIDDEN_BALANCE
        } else {
            item.balanceFormat
        }

        if (item.testnet) {
            rateView.visibility = View.GONE
            balanceFiatView.visibility = View.GONE
        } else {
            balanceFiatView.visibility = View.VISIBLE
            if (item.hiddenBalance) {
                balanceFiatView.text = HIDDEN_BALANCE
            } else {
                balanceFiatView.text = item.fiatFormat
            }
            if (item.staked) {
                rateView.isVisible = true
                rateView.text = item.rate
            } else {
                setRate(item.rate, item.rateDiff24h, item.verified)
            }
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