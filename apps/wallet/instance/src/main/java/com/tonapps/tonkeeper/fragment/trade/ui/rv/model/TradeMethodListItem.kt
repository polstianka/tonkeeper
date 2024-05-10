package com.tonapps.tonkeeper.fragment.trade.ui.rv.model

import com.tonapps.tonkeeper.fragment.trade.ui.rv.TradeAdapter
import com.tonapps.uikit.list.BaseListItem

data class TradeMethodListItem(
    val id: String,
    val isChecked: Boolean,
    val title: String,
    val iconUrl: String
) : BaseListItem(TradeAdapter.TYPE_TRADE_METHOD)
