package com.tonapps.tonkeeper.dialog.trade.list

data class PaymentItem(
    val type: PaymentType,
    val text: String,
    val iconIds: List<Int>,
    val isSelected: Boolean = false,
)
