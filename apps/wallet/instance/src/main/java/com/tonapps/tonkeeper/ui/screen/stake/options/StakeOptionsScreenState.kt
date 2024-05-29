package com.tonapps.tonkeeper.ui.screen.stake.options

import com.tonapps.tonkeeper.App
import com.tonapps.wallet.data.account.legacy.WalletLegacy
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import io.tonapi.models.PoolImplementation
import io.tonapi.models.PoolImplementationType
import io.tonapi.models.PoolInfo
import uikit.mvi.UiState

data class StakeOptionsScreenState( //todo rm extra
    val wallet: WalletLegacy? = null,
    val amount: Float = 0f,
    val currency: WalletCurrency = App.settings.currency,
    val available: CharSequence = "",
    val rate: CharSequence = "0 ${App.settings.currency.code}",
    val insufficientBalance: Boolean = false,
    val remaining: CharSequence = "",
    val maxActive: Boolean = false,
    val tokens: List<AccountTokenEntity> = emptyList(),
    val selectedTokenAddress: String = WalletCurrency.TON.code,
    val stakingPools: List<PoolInfo> = emptyList(),
    val optionItems: List<OptionItem> = emptyList(),
    val selectedPool: PoolInfo? = null,
    val poolImplementation: PoolImplementation? = null
) : UiState() {

    val selectedToken: AccountTokenEntity?
        get() = tokens.firstOrNull { it.address == selectedTokenAddress }

    val decimals: Int
        get() = selectedToken?.decimals ?: 9

    val selectedTokenCode: String
        get() = selectedToken?.symbol ?: "TON"
}