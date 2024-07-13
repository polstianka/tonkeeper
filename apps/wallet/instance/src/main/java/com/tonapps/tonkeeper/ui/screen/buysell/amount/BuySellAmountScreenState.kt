package com.tonapps.tonkeeper.ui.screen.buysell.amount

import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.api.buysell.BuySellType
import com.tonapps.tonkeeper.api.buysell.TradeType
import com.tonapps.wallet.data.account.legacy.WalletLegacy
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import uikit.mvi.UiState

// todo remove extra
data class BuySellAmountScreenState(
    val wallet: WalletLegacy? = null,
    val amount: Float = 0f,
    val minAmount: Int = 5,
    val currency: WalletCurrency = App.settings.currency,
    val available: CharSequence = "",
    val rate: CharSequence = "0 ${App.settings.currency.code}",
    val insufficientBalance: Boolean = false,
    val remaining: CharSequence = "",
    val canContinue: Boolean = false,
    val maxActive: Boolean = false,
    val tokens: List<AccountTokenEntity> = emptyList(),
    val selectedTokenAddress: String = WalletCurrency.TON.code,
    val types: List<BuySellType> = emptyList(),
    val selectedType: BuySellType? = null,
    val tradeType: TradeType = TradeType.BUY
) : UiState() {

    val selectedToken: AccountTokenEntity?
        get() = tokens.firstOrNull { it.address == selectedTokenAddress }

    val decimals: Int
        get() = selectedToken?.decimals ?: 9

    val selectedTokenCode: String
        get() = selectedToken?.symbol ?: "TON"
}