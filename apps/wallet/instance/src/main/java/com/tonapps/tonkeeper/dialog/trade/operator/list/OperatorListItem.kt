package com.tonapps.tonkeeper.dialog.trade.operator.list

import com.tonapps.tonkeeper.dialog.trade.operator.OperatorItem

class OperatorListItem(
    val body: OperatorItem,
    override val position: com.tonapps.uikit.list.ListCell.Position,
) : com.tonapps.uikit.list.BaseListItem(), com.tonapps.uikit.list.ListCell {
    val id = body.id
    val rate: Double? = body.rate
    val fiatCurrency: String = body.fiatCurrency
    val isSelected: Boolean = body.isSelected
    val iconUrl: String = body.iconUrl
    val title: String = body.title
    val subtitle: String = body.subtitle
}
