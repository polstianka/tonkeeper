package com.tonapps.tonkeeper.fragment.stake.pick_option.rv

import android.view.ViewGroup
import androidx.core.view.isVisible
import uikit.extensions.setThrottleClickListener

class StakingOptionHolder(
    parent: ViewGroup,
    val onClicked: (StakingOptionListItem) -> Unit
) : BaseStakingOptionHolder<StakingOptionListItem>(parent) {

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