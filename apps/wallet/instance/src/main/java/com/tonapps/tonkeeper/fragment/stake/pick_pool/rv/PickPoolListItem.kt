package com.tonapps.tonkeeper.fragment.stake.pick_pool.rv

import com.tonapps.tonkeeper.core.TextWrapper
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell

data class PickPoolListItem(
    val iconUrl: String,
    val title: String,
    val subtitle: TextWrapper,
    val isChecked: Boolean,
    val address: String,
    val position: ListCell.Position,
    val isMaxApy: Boolean
) : BaseListItem(1)