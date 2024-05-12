package com.tonapps.tonkeeper.fragment.trade.pick_operator.rv.model

import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell

data class PaymentOperatorListItem(
    val id: String,
    val iconUrl: String,
    val title: String,
    val rate: String,
    val isPicked: Boolean,
    val isBest: Boolean,
    val position: ListCell.Position
) : BaseListItem(1)