package com.tonapps.tonkeeper.ui.screen.swap.choose

import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.api.swap.StonfiSwapAsset
import com.tonapps.wallet.data.account.legacy.WalletLegacy
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import uikit.mvi.UiState

data class SwapChooseScreenState(
    val wallet: WalletLegacy? = null,
    val amount: Float = 0f,
    val currency: WalletCurrency = App.settings.currency,
    val available: CharSequence = "",
    val rate: CharSequence = "0 ${App.settings.currency.code}",
    val insufficientBalance: Boolean = false,
    val remaining: CharSequence = "",
    val canContinue: Boolean = false,
    val maxActive: Boolean = false,
    val tokens: List<AccountTokenEntity> = emptyList(),
    val selectedTokenAddress: String = WalletCurrency.TON.code,
    val assets: List<StonfiSwapAsset> = emptyList(),
    val filteredAssets: List<StonfiSwapAsset> = emptyList(),
    val suggestedAssets: List<StonfiSwapAsset> = emptyList(),
    val chosen: StonfiSwapAsset? = null
) : UiState()