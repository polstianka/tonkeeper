package com.tonapps.tonkeeper.fragment.stake.pick_pool.rv

import android.view.ViewGroup
import androidx.core.view.isVisible
import coil.transform.RoundedCornersTransformation
import com.tonapps.tonkeeper.core.loadUri
import com.tonapps.tonkeeper.core.toString
import com.tonapps.tonkeeper.fragment.stake.pick_option.rv.BaseStakingOptionHolder
import uikit.extensions.dp
import uikit.extensions.setThrottleClickListener

class PickPoolHolder(
    parent: ViewGroup,
    val onClick: (PickPoolListItem) -> Unit
) : BaseStakingOptionHolder<PickPoolListItem>(parent) {

    override fun onBind(item: PickPoolListItem) {
        icon.loadUri(item.iconUri, RoundedCornersTransformation(22f.dp))
        title.text = item.title
        subtitle.text = context.toString(item.subtitle)
        radioButton.isChecked = item.isChecked
        baseItemView.position = item.position
        baseItemView.setThrottleClickListener { onClick(item) }
        chip.isVisible = item.isMaxApy
    }
}