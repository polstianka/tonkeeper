package com.tonapps.tonkeeper.ui.screen.buysell.operator

import com.tonapps.tonkeeper.core.fiat.models.FiatItem
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.entity.OperatorBuyRateEntity

data class OperatorScreenState(
    val continueState: ContinueState = ContinueState.DISABLE,
    val currency: WalletCurrency? = null,
    val currencyNameResId: Int? = null,
    val fiatItems: List<FiatItem> = emptyList(),
    val operatorRates: Map<String, OperatorBuyRateEntity> = emptyMap(),
) {

    sealed class ContinueState {
        object NEXT : ContinueState()
        object NO_OPERATOR_AVAILABLE : ContinueState()
        object DISABLE : ContinueState()
    }

    companion object {
        val initState = OperatorScreenState()
    }
}