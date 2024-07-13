package com.tonapps.tonkeeper.ui.screen.stake.options

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import com.tonapps.tonkeeper.ui.screen.swap.CellBackgroundDrawable
import com.tonapps.tonkeeperx.R
import io.tonapi.models.PoolImplementationType
import io.tonapi.models.PoolInfo
import uikit.extensions.dp
import uikit.extensions.round
import uikit.widget.FrescoView

class StakeOptionsListHolder(parent: ViewGroup, val listener: (PoolImplementationType?, PoolInfo?) -> Unit) : StakeOptionsHolder<OptionItem.OptionList>(parent, R.layout.view_cell_stake) {

    private val titleView = findViewById<AppCompatTextView>(R.id.title)
    private val iconView = findViewById<FrescoView>(R.id.icon)
    private val rateView = findViewById<AppCompatTextView>(R.id.rate)
    private val radioView = findViewById<AppCompatRadioButton>(R.id.radio)
    private val chevronView = findViewById<AppCompatImageView>(R.id.chevron)

    init {
        iconView.round(22.dp)
    }

    override fun onBind(item: OptionItem.OptionList) {
        radioView.isVisible = false
        chevronView.isVisible = true
        itemView.setOnClickListener { listener.invoke(item.implementationType, null) }
        itemView.background = CellBackgroundDrawable.create(context, item.position, item.count)
        iconView.setImageResource(item.icon)
        titleView.text = item.implementation.name
        rateView.text = item.implementation.description
    }

}