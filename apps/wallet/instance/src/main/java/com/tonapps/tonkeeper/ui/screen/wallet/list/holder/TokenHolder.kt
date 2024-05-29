package com.tonapps.tonkeeper.ui.screen.wallet.list.holder

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import com.tonapps.blockchain.Coin
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.api.icon
import com.tonapps.tonkeeper.extensions.buildRateString
import com.tonapps.tonkeeper.fragment.jetton.JettonScreen
import com.tonapps.tonkeeper.fragment.staking.StakingScreen
import com.tonapps.tonkeeper.helper.flow.CountdownTimer
import com.tonapps.tonkeeper.ui.screen.wallet.list.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.accentOrangeColor
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.wallet.api.entity.BalanceStakeEntity
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.core.HIDDEN_BALANCE
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import uikit.extensions.drawable
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.FrescoView
import java.math.BigInteger

class TokenHolder(parent: ViewGroup): Holder<Item.Token>(parent, R.layout.view_cell_jetton_optimized_2) {

    private val iconView = findViewById<FrescoView>(R.id.icon)
    private val iconLabelView = findViewById<AppCompatImageView>(R.id.icon_label)
    private val titleView = findViewById<AppCompatTextView>(R.id.title)
    private val rateView = findViewById<AppCompatTextView>(R.id.rate)
    private val balanceView = findViewById<AppCompatTextView>(R.id.balance)
    private val balanceFiatView = findViewById<AppCompatTextView>(R.id.balance_currency)
    private val labelViewWrapper = findViewById<FrameLayout>(R.id.icon_label_wrapper)
    private val labelView = findViewById<AppCompatTextView>(R.id.label)
    private val pendingWithdraw = findViewById<AppCompatTextView>(R.id.staking_pending_withdraw)
    private val pendingDeposit = findViewById<AppCompatTextView>(R.id.staking_pending_deposit)
    private val pendingTake = findViewById<AppCompatTextView>(R.id.staking_pending_take)

    private val cycleEndFlow = MutableStateFlow<Long>(0)
    private val cycleEndCountdown = CountdownTimer.create(cycleEndFlow)

    private var scope: CoroutineScope? = null

    override fun onBind(item: Item.Token) {
        scope?.cancel()
        scope = null

        itemView.background = item.position.drawable(context)
        itemView.setOnClickListener {
            if (item.stake != null) {
                context.navigation?.add(StakingScreen.newInstance(item.stake.pool.address, item.stake.pool.name))
            } else {
                context.navigation?.add(JettonScreen.newInstance(item.address, item.name, item.symbol))
            }
        }
        labelView.isVisible = item.address == TokenEntity.TETHER_USDT_ADDRESS
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
            setRate(item.rate, item.rateDiff24h, item.verified)
        }

        iconLabelView.clipToOutline = true
        if (item.stake != null) {
            pendingTake.isVisible = item.stake.readyWithdrawNano > BigInteger.ZERO
            pendingTake.text = context.getString(Localization.staking_ready_withdraw,
                CurrencyFormatter.format(TokenEntity.TON.symbol, Coin.toCoins(item.stake.readyWithdrawNano, TokenEntity.TON.decimals))
            )

            pendingDeposit.isVisible = item.stake.pendingDepositNano > BigInteger.ZERO
            pendingWithdraw.isVisible = item.stake.pendingWithdrawNano > BigInteger.ZERO

            if (item.stake.pendingWithdrawNano > BigInteger.ZERO || item.stake.pendingDepositNano > BigInteger.ZERO) {
                setPendings(item.stake, CountdownTimer.remaining(item.stake.pool.cycleEnd))
                cycleEndFlow.value = item.stake.pool.cycleEnd

                val s = CoroutineScope(Dispatchers.Main)
                cycleEndCountdown.onEach{ stamp ->
                    setPendings(item.stake, stamp)
                }.launchIn(s)
                scope = s
            } else {
                cycleEndFlow.value = 0
            }

            labelViewWrapper.isVisible = true
            iconLabelView.setImageResource(item.stake.pool.implementation.type.icon)
            titleView.text = getString(com.tonapps.wallet.localization.R.string.staking_staked)
            rateView.text = item.stake.pool.name
            rateView.setTextColor(context.textSecondaryColor)
            iconView.setImageURI(TokenEntity.TON.imageUri, this)
        } else {
            pendingTake.isVisible = false
            pendingDeposit.isVisible = false
            pendingWithdraw.isVisible = false

            iconView.setImageURI(item.iconUri, this)
            labelViewWrapper.isVisible = false
        }
    }

    private fun setPendings(item: BalanceStakeEntity, remaining: Long) {
        if (!itemView.isAttachedToWindow || remaining <= 0) {
            scope?.cancel()
            scope = null
        }

        pendingDeposit.text = context.getString(Localization.staking_pending_deposit,
            CurrencyFormatter.format(TokenEntity.TON.symbol, Coin.toCoins(item.pendingDepositNano, TokenEntity.TON.decimals)),
            CountdownTimer.format(remaining)
        )
        pendingWithdraw.text = context.getString(Localization.staking_pending_withdraw,
            CurrencyFormatter.format(TokenEntity.TON.symbol, Coin.toCoins(item.pendingWithdrawNano, TokenEntity.TON.decimals)),
            CountdownTimer.format(remaining)
        )
    }

    override fun onUnbind() {
        super.onUnbind()
        scope?.cancel()
        scope = null
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