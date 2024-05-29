package com.tonapps.tonkeeper.ui.screen.buysell.confirm

import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.api.buysell.BuySellOperator
import com.tonapps.tonkeeper.api.buysell.BuySellType
import com.tonapps.tonkeeper.api.buysell.TradeType
import com.tonapps.wallet.data.core.WalletCurrency
import uikit.mvi.UiState

data class BuySellConfirmScreenState(
    val currency: WalletCurrency = App.settings.currency,
    val selectedOperator: BuySellOperator? = null,
    val buySellType: BuySellType? = null,
    val amount: Float = 0f,
    val amountCrypto: Float = 0f,
    val tradeType: TradeType = TradeType.BUY,
    val loading: Boolean = false,
    val url: String = "",
    val rate: String = "",
    val canContinue: Boolean = false,
    val error1: Boolean = false,
    val error2: Boolean = false,
    val cryptoBalance: Float = 0f
) : UiState()