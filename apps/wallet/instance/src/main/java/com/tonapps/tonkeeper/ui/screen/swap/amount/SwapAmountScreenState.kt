package com.tonapps.tonkeeper.ui.screen.swap.amount

import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.api.swap.StonfiSwapAsset
import com.tonapps.tonkeeper.api.swap.SwapSimulateData
import com.tonapps.wallet.data.account.legacy.WalletLegacy
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import uikit.mvi.UiState

data class SwapAmountScreenState(
    val wallet: WalletLegacy? = null,
    val currency: WalletCurrency = App.settings.currency,
    val available: CharSequence = "",
    val availableRec: CharSequence = "",
    val rate: CharSequence = "0 ${App.settings.currency.code}",
    val rateRec: CharSequence = "0 ${App.settings.currency.code}",
    val insufficientBalance: Boolean = false,
    val insufficientBalanceRec: Boolean = false,
    val remaining: CharSequence = "",
    val canContinue: Boolean = false,
    val maxActive: Boolean = false,
    val tokens: List<AccountTokenEntity> = emptyList(),
    val selectedTokenAddress: String = WalletCurrency.TON.code,
    val swapFrom: StonfiSwapAsset? = null,
    val swapTo: StonfiSwapAsset? = null,
    val swapFromAmount: String = "0",
    val swapToAmount: String = "",
    val amount: Float = 0f,
    val amountRec: Float = 0f,
    val simulateData: SwapSimulateData? = null,
    val loadingSimulation: Boolean = false,
    val initialLoading: Boolean = false,
    val swapRate: CharSequence = "",
    val slippage: Float = 0.01f,
    val expertMode: Boolean = false
) : UiState() {

    val selectedToken: AccountTokenEntity?
        get() = tokens.firstOrNull { it.symbol == swapFrom?.symbol }

    val selectedTokenRec: AccountTokenEntity?
        get() = tokens.firstOrNull { it.symbol == swapTo?.symbol }

    val decimals: Int
        get() = swapFrom?.decimals ?: 9

    val decimalsRec: Int
        get() = swapTo?.decimals ?: 9

    val selectedTokenCode: String
        get() = swapFrom?.symbol ?: ""

    val selectedTokenCodeRec: String
        get() = swapTo?.symbol ?: ""
}