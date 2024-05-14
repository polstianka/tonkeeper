package com.tonapps.tonkeeper.fragment.stake.pick_option.rv

import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.view.isVisible
import com.facebook.drawee.view.SimpleDraweeView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.BaseListHolder
import uikit.extensions.setThrottleClickListener
import uikit.widget.item.BaseItemView

class StakingOptionHolder(
    parent: ViewGroup,
    val onClicked: (StakingOptionListItem) -> Unit
) : BaseListHolder<StakingOptionListItem>(parent, R.layout.view_item_staking_option) {

    private val baseItemView: BaseItemView = itemView as BaseItemView
    private val icon: SimpleDraweeView = findViewById(R.id.view_item_staking_option_icon)
    private val title: TextView = findViewById(R.id.view_item_staking_option_title)
    private val subtitle: TextView = findViewById(R.id.view_item_staking_option_subtitle)
    private val radioButton: RadioButton = findViewById(R.id.view_item_staking_option_radiobutton)
    private val chip: View = findViewById(R.id.view_item_staking_option_chip)
    override fun onBind(item: StakingOptionListItem) {
        baseItemView.position = item.position
        icon.setImageURI(item.iconUrl)
        title.text = item.title
        subtitle.text = item.subtitle
        chip.isVisible = item.isMaxApy
        radioButton.isChecked = item.isPicked
        baseItemView.setThrottleClickListener { onClicked(item) }
    }
}