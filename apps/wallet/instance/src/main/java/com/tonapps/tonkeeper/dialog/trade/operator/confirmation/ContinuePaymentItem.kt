package com.tonapps.tonkeeper.dialog.trade.operator.confirmation

import com.tonapps.tonkeeper.core.fiat.models.FiatSuccessUrlPattern

data class ContinuePaymentItem(
    val url: String,
    val pattern: FiatSuccessUrlPattern?,
)
