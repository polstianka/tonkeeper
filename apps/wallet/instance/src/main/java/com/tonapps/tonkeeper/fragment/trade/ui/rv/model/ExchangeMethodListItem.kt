package com.tonapps.tonkeeper.fragment.trade.ui.rv.model

import com.tonapps.tonkeeper.fragment.trade.domain.model.ExchangeMethod
import com.tonapps.tonkeeper.fragment.trade.ui.rv.TradeAdapter
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell

data class ExchangeMethodListItem(
    val id: String,
    val isChecked: Boolean,
    val title: String,
    val iconUrl: String,
    val position: ListCell.Position,
    val method: ExchangeMethod
) : BaseListItem(TradeAdapter.TYPE_TRADE_METHOD)
