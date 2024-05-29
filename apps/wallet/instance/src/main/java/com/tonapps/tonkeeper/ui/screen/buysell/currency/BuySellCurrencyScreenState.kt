package com.tonapps.tonkeeper.ui.screen.buysell.currency

import com.tonapps.tonkeeper.App
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.tonkeeper.ui.screen.settings.currency.list.Item
import uikit.mvi.UiState

data class BuySellCurrencyScreenState(
    val currency: WalletCurrency = App.settings.currency,
    val currencyList: List<Item> = emptyList()
) : UiState()