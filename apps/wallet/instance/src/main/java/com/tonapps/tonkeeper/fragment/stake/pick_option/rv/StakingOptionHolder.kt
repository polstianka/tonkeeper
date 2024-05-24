package com.tonapps.tonkeeper.fragment.stake.pick_option.rv

import android.view.ViewGroup
import androidx.core.view.isVisible
import coil.transform.RoundedCornersTransformation
import com.tonapps.tonkeeper.core.loadUri
import uikit.extensions.dp
import uikit.extensions.setThrottleClickListener

class StakingOptionHolder(
    parent: ViewGroup,
    val onClicked: (StakingOptionListItem) -> Unit
) : BaseStakingOptionHolder<StakingOptionListItem>(parent) {

    override fun onBind(item: StakingOptionListItem) {
        baseItemView.position = item.position

        icon.loadUri(item.iconUrl, RoundedCornersTransformation(22f.dp))

        title.text = item.title
        subtitle.text = item.subtitle
        chip.isVisible = item.isMaxApy
        radioButton.isChecked = item.isPicked
        baseItemView.setThrottleClickListener { onClicked(item) }
    }
}