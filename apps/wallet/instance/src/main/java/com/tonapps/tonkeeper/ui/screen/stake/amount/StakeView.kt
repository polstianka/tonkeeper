package com.tonapps.tonkeeper.ui.screen.stake.amount

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.api.icon
import com.tonapps.tonkeeper.ui.screen.stake.StakingRepository
import com.tonapps.tonkeeperx.R
import io.tonapi.models.PoolInfo
import uikit.extensions.dp
import uikit.extensions.round
import uikit.extensions.scale
import uikit.widget.FrescoView

class StakeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle) {

    private val iconView: FrescoView
    private val titleView: AppCompatTextView
    private val rateView: AppCompatTextView
    private val chevron: AppCompatImageView
    private val maxapy: View

    init {
        inflate(context, R.layout.view_cell_stake, this)
        iconView = findViewById(R.id.icon)
        maxapy = findViewById(R.id.maxapy)
        titleView = findViewById(R.id.title)
        rateView = findViewById(R.id.rate)
        chevron = findViewById(R.id.chevron)
        chevron.setImageResource(R.drawable.chevron_updown)
        chevron.scale = 1.5f
        chevron?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            this.marginEnd = 8.dp
        }
        iconView.round(22.dp)
    }

    fun setPool(item: PoolInfo) {
        iconView.setImageResource(item.implementation.icon)
        titleView.text = item.name
        rateView.text = "APY ≈ ${CurrencyFormatter.format("%", item.apy)}"
        maxapy.isVisible = StakingRepository.maxApy == item.apy
    }
}