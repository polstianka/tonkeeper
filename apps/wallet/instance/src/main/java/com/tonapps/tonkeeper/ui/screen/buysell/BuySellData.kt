package com.tonapps.tonkeeper.ui.screen.buysell

import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.api.buysell.BuySellOperator
import com.tonapps.tonkeeper.api.buysell.BuySellType
import com.tonapps.tonkeeper.api.buysell.TradeType
import com.tonapps.wallet.data.core.WalletCurrency

data class BuySellData(
    val currency: WalletCurrency = App.settings.currency,
    val selectedOperator: BuySellOperator? = null,
    val buySellType: BuySellType? = null,
    val amount: Float = 0f,
    val tradeType: TradeType = TradeType.BUY,
    val cryptoBalance: Float = 0f
)