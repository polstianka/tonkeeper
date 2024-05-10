package com.tonapps.tonkeeper.fragment.trade.ui

import com.tonapps.uikit.list.BaseListItem

data class TradeMethodListItem(
    val id: String,
    val isChecked: Boolean,
    val title: String,
    val iconUrl: String
) : BaseListItem(1)
