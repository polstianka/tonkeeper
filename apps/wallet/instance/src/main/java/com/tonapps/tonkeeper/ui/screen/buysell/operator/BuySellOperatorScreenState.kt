package com.tonapps.tonkeeper.ui.screen.buysell.operator

import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.api.buysell.BuySellOperator
import com.tonapps.tonkeeper.api.buysell.BuySellType
import com.tonapps.tonkeeper.api.buysell.TradeType
import com.tonapps.wallet.data.core.WalletCurrency
import uikit.mvi.UiState

data class BuySellOperatorScreenState(
    val currency: WalletCurrency = App.settings.currency,
    val operators: List<BuySellOperator> = emptyList(),
    val selectedOperator: BuySellOperator? = null,
    val buySellType: BuySellType? = null,
    val tradeType: TradeType = TradeType.BUY,
    val loading: Boolean = false
) : UiState()