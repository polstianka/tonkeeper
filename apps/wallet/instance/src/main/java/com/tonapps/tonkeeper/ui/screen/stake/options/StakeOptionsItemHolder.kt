package com.tonapps.tonkeeper.ui.screen.stake.options

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.api.icon
import com.tonapps.tonkeeper.ui.screen.stake.StakingRepository
import com.tonapps.tonkeeper.ui.screen.swap.CellBackgroundDrawable
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.UIKitColor
import com.tonapps.uikit.color.resolveColor
import io.tonapi.models.PoolImplementationType
import io.tonapi.models.PoolInfo
import uikit.extensions.dp
import uikit.extensions.round
import uikit.widget.FrescoView

class StakeOptionsItemHolder(parent: ViewGroup, val listener: (PoolImplementationType?, PoolInfo?) -> Unit) : StakeOptionsHolder<OptionItem.Option>(parent, R.layout.view_cell_stake) {

    private val titleView = findViewById<AppCompatTextView>(R.id.title)
    private val iconView = findViewById<FrescoView>(R.id.icon)
    private val rateView = findViewById<AppCompatTextView>(R.id.rate)
    private val radioView = findViewById<AppCompatRadioButton>(R.id.radio)
    private val chevronView = findViewById<AppCompatImageView>(R.id.chevron)
    private val maxapy = findViewById<View>(R.id.maxapy)

    init {
        iconView.round(22.dp)
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
            ),
            intArrayOf(
                context.resolveColor(UIKitColor.buttonTertiaryBackgroundColor),
                context.resolveColor(UIKitColor.buttonPrimaryBackgroundColor),
            )
        )
        radioView.buttonTintList = colorStateList
    }

    @SuppressLint("SetTextI18n")
    override fun onBind(item: OptionItem.Option) {
        radioView.isChecked = item.chosen
        radioView.isVisible = true
        chevronView.isVisible = false
        itemView.setOnClickListener { listener.invoke(null, item.poolInfo) }
        itemView.background = CellBackgroundDrawable.create(context, item.position, item.count)
        iconView.setImageResource(item.poolInfo.implementation.icon)
        titleView.text = item.poolInfo.name
        rateView.text = "APY ≈ ${CurrencyFormatter.format("%", item.poolInfo.apy)}"
        maxapy.isVisible = StakingRepository.maxApy == item.poolInfo.apy
    }

}