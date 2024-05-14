package com.tonapps.tonkeeper.fragment.stake

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.core.TextWrapper
import com.tonapps.tonkeeper.core.emit
import com.tonapps.tonkeeper.extensions.formattedRate
import com.tonapps.tonkeeper.fragment.trade.domain.GetRateFlowCase
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.TokenRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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
    private val availableText = balance.map {
        val amount = CurrencyFormatter.format(it.balance.token.name, it.balance.value)
        TextWrapper.StringResource(LocalizationR.string.stake_fragment_available_mask, amount)
    }
    private val isValid = combine(balance, amount) { balance, amount ->
        balance.balance.value >= amount
    }
    val labelTextColorAttribute = isValid.map { isValid ->
        if (isValid) {
            com.tonapps.uikit.color.R.attr.textSecondaryColor
        } else {
            com.tonapps.uikit.color.R.attr.accentRedColor
        }
    }
    val labelText = isValid.flatMapLatest { isValid ->
        if (isValid) {
            availableText
        } else {
            flowOf(
                TextWrapper.StringResource(LocalizationR.string.insufficient_balance)
            )
        }
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

    fun onMaxClicked() = viewModelScope.launch {
        val balance = balance.first().balance.value
        _events.emit(StakeEvent.SetInputValue(balance))
    }

    fun onDropdownClicked() {
        Log.wtf("###", "onDropdownClicked")
    }
}