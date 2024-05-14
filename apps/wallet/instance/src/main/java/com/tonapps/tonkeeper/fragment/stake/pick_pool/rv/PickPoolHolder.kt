package com.tonapps.tonkeeper.fragment.stake.pick_pool.rv

import android.view.ViewGroup
import com.tonapps.tonkeeper.fragment.stake.pick_option.rv.BaseStakingOptionHolder
import uikit.extensions.setThrottleClickListener

class PickPoolHolder(
    parent: ViewGroup,
    val onClick: (PickPoolListItem) -> Unit
) : BaseStakingOptionHolder<PickPoolListItem>(parent) {

    override fun onBind(item: PickPoolListItem) {
        icon.setImageURI(item.iconUrl)
        title.text = item.title
        subtitle.text = item.subtitle
        radioButton.isChecked = item.isChecked
        baseItemView.position = item.position
        baseItemView.setThrottleClickListener { onClick(item) }
    }
}