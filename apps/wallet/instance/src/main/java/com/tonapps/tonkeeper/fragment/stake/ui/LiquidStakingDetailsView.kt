package com.tonapps.tonkeeper.fragment.stake.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import coil.transform.RoundedCornersTransformation
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.core.loadUri
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPoolLiquidJetton
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.data.rates.entity.RateEntity
import uikit.extensions.applySelectableBgContent
import uikit.extensions.dp
import uikit.extensions.setThrottleClickListener
import uikit.widget.ColumnLayout
import java.math.BigDecimal

class LiquidStakingDetailsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : ColumnLayout(context, attrs, defStyle){

    private val icon: ImageView?
        get() = findViewById(R.id.view_liquid_staking_details_icon)
    private val title: TextView?
        get() = findViewById(R.id.view_liquid_staking_details_title)
    private val subtitle: TextView?
        get() = findViewById(R.id.view_liquid_staking_details_subtitle)
    private val description: TextView?
        get() = findViewById(R.id.view_liquid_staking_details_description)
    private val amountCrypto: TextView?
        get() = findViewById(R.id.view_liquid_staking_details_amount_crypto)
    private val amountFiat: TextView?
        get() = findViewById(R.id.view_liquid_staking_details_amount_fiat)
    private val clickableArea: View?
        get() = findViewById(R.id.view_liquid_staking_details_clickable_area)

    init {
        inflate(context, R.layout.view_liquid_staking_details, this)
        isVisible = false
        clickableArea?.applySelectableBgContent()
    }

    fun applyLiquidJetton(liquidJetton: StakingPoolLiquidJetton?) {

        isVisible = liquidJetton != null
        liquidJetton ?: return
        icon?.loadUri(liquidJetton.iconUri, RoundedCornersTransformation(24f.dp))
        title?.text = liquidJetton.symbol
        liquidJetton.price?.let {
            subtitle?.text = CurrencyFormatter.format(
                liquidJetton.currency.code,
                liquidJetton.price
            )
        }
        description?.text = resources.getString(
            com.tonapps.wallet.localization.R.string.liquid_staking_description,
            liquidJetton.poolName,
            liquidJetton.symbol,
            liquidJetton.symbol
        )
    }

    fun setAmount(amount: BigDecimal, rate: RateEntity) {
        amountCrypto?.text = CurrencyFormatter.format("", amount)
        val amountFiat = amount * rate.value
        this.amountFiat?.text = CurrencyFormatter.format(
            rate.currency.code,
            amountFiat
        )
    }

    fun setOnTokenAreaClickListener(action: () -> Unit) {
        clickableArea?.setThrottleClickListener { action() }
    }
}