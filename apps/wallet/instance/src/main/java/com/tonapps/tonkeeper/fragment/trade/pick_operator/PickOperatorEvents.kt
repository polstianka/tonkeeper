package com.tonapps.tonkeeper.fragment.trade.pick_operator

import com.tonapps.tonkeeper.core.fiat.models.FiatSuccessUrlPattern

sealed class PickOperatorEvents {
    object NavigateBack : PickOperatorEvents()
    object CloseFlow : PickOperatorEvents()
    data class PickCurrency(
        val paymentMethodId: String,
        val pickedCurrencyCode: String
    ) : PickOperatorEvents()

    data class NavigateToWebView(
        val url: String,
        val successUrlPattern: FiatSuccessUrlPattern?
    ) : PickOperatorEvents()
}