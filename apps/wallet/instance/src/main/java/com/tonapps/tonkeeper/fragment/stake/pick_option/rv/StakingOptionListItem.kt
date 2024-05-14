package com.tonapps.tonkeeper.fragment.stake.pick_option.rv

import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingServiceType
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell

class StakingOptionListItem(
    val iconUrl: String,
    val title: String,
    val isMaxApy: Boolean,
    val subtitle: String,
    val isPicked: Boolean,
    val stakingServiceType: StakingServiceType,
    val position: ListCell.Position
) : BaseListItem(1) {
}