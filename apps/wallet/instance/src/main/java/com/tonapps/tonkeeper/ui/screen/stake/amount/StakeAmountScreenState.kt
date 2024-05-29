package com.tonapps.tonkeeper.ui.screen.stake.amount

import com.tonapps.tonkeeper.App
import com.tonapps.wallet.data.account.legacy.WalletLegacy
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import io.tonapi.models.PoolImplementation
import io.tonapi.models.PoolInfo
import uikit.mvi.UiState

// todo remove extra
data class StakeAmountScreenState(
    val wallet: WalletLegacy? = null,
    val amount: Float = 0f,
    val currency: WalletCurrency = App.settings.currency,
    val available: CharSequence = "",
    val rate: CharSequence = "0.00 ${App.settings.currency.code}",
    val insufficientBalance: Boolean = false,
    val remaining: CharSequence = "",
    val canContinue: Boolean = false,
    val maxActive: Boolean = false,
    val tokens: List<AccountTokenEntity> = emptyList(),
    val selectedTokenAddress: String = WalletCurrency.TON.code,
    val stakingPools: List<PoolInfo> = emptyList(),
    val selectedPool: PoolInfo? = null,
    val implMap: Map<String, PoolImplementation> = emptyMap(),
    val selectedImpl: PoolImplementation? = null,
    val loading: Boolean = false,
    val isUnstake: Boolean = false,
    val address: String = "",
    val stakingToken: AccountTokenEntity? = null
) : UiState() {

    val selectedToken: AccountTokenEntity?
        get() = tokens.firstOrNull { it.address == selectedTokenAddress }

    val decimals: Int
        get() = selectedToken?.decimals ?: 9

    val selectedTokenCode: String
        get() = selectedToken?.symbol ?: "TON"
}