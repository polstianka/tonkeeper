package com.tonapps.tonkeeper.fragment.send.amount

import com.tonapps.tonkeeper.App
import com.tonapps.wallet.data.core.WalletCurrency
import io.tonapi.models.JettonBalance
import com.tonapps.wallet.data.account.legacy.WalletLegacy
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import uikit.mvi.UiState

data class AmountScreenState(
    val wallet: WalletLegacy? = null,
    val amount: Double = 0.0,
    val currency: WalletCurrency = WalletCurrency.TON,
    val available: CharSequence = "",
    val rate: CharSequence = "0",
    val insufficientBalance: Boolean = false,
    val remaining: CharSequence = "",
    val canContinue: Boolean = false,
    val maxActive: Boolean = false,
    val tokens: List<AccountTokenEntity> = emptyList(),
    val selectedTokenAddress: String = WalletCurrency.TON.code
): UiState() {

    val selectedToken: AccountTokenEntity?
        get() = tokens.firstOrNull { it.address == selectedTokenAddress }

    val decimals: Int
        get() = selectedToken?.decimals ?: 9

    val selectedTokenCode: String
        get() = selectedToken?.symbol ?: "TON"
}