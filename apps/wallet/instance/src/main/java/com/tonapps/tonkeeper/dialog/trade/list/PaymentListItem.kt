package com.tonapps.tonkeeper.dialog.trade.list

data class PaymentListItem(
    val body: PaymentItem,
    override val position: com.tonapps.uikit.list.ListCell.Position,
) : com.tonapps.uikit.list.BaseListItem(), com.tonapps.uikit.list.ListCell {
    val paymentType = body.type
    val title: String
        get() = body.text

    val iconIds: List<Int>
        get() = body.iconIds

    val isSelected = body.isSelected
}
