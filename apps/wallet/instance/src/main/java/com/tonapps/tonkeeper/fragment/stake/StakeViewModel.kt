package com.tonapps.tonkeeper.fragment.stake

import android.util.Log
import androidx.lifecycle.ViewModel
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.core.TextWrapper
import com.tonapps.tonkeeper.core.emit
import com.tonapps.tonkeeper.core.observeFlow
import com.tonapps.tonkeeper.extensions.formattedRate
import com.tonapps.tonkeeper.fragment.trade.domain.GetRateFlowCase
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.account.legacy.WalletManager
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.TokenRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import com.tonapps.wallet.localization.R as LocalizationR

@OptIn(ExperimentalCoroutinesApi::class)
class StakeViewModel(
    settingsRepository: SettingsRepository,
    getRateFlowCase: GetRateFlowCase,
    walletRepository: WalletRepository,
    tokenRepository: TokenRepository
) : ViewModel() {

    companion object {
        const val TOKEN_TON = "TON"
        const val isTestnet = false // todo
    }

    private val _events = MutableSharedFlow<StakeEvent>()
    val events: Flow<StakeEvent>
        get() = _events
    private val currency = settingsRepository.currencyFlow
    private val exchangeRate = currency.flatMapLatest { getRateFlowCase.execute(it) }
    private val amount = MutableStateFlow(0f)
    private val activeWallet = walletRepository.activeWalletFlow
    private val balance = combine(activeWallet, currency) { wallet, currency ->
        tokenRepository.get(currency, wallet.accountId, isTestnet)
            .firstOrNull { it.isTon }
    }
        .filterNotNull()
    val fiatAmount = formattedRate(exchangeRate, amount, TOKEN_TON)
    val available = balance.map {
        val amount = CurrencyFormatter.format(it.balance.token.name, it.balance.value)
        TextWrapper.StringResource(LocalizationR.string.stake_fragment_available_mask, amount)
    }

    fun onCloseClicked() {
        emit(_events, StakeEvent.NavigateBack)
    }

    fun onInfoClicked() {
        emit(_events, StakeEvent.ShowInfo)
    }

    fun onAmountChanged(amount: Float) {
        if (amount == this.amount.value) return
        this.amount.value = amount
    }

    fun onMaxClicked() {
        Log.wtf("###", "onMaxClicked")
    }
}