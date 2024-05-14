package com.tonapps.tonkeeper.fragment.stake.pick_pool.rv

import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell

data class PickPoolListItem(
    val iconUrl: String,
    val title: String,
    val subtitle: String,
    val isChecked: Boolean,
    val accountNumber: String,
    val position: ListCell.Position
) : BaseListItem(1)