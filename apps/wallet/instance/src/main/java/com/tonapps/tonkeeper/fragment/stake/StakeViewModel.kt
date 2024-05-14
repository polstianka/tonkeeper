package com.tonapps.tonkeeper.fragment.stake

import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.core.emit
import com.tonapps.tonkeeper.extensions.formattedRate
import com.tonapps.tonkeeper.fragment.trade.domain.GetRateFlowCase
import com.tonapps.wallet.data.settings.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class StakeViewModel(
    settingsRepository: SettingsRepository,
    getRateFlowCase: GetRateFlowCase,
) : ViewModel() {

    companion object {
        const val TOKEN_TON = "TON"
    }
    private val _events = MutableSharedFlow<StakeEvent>()
    val events: Flow<StakeEvent>
        get() = _events
    private val currency = settingsRepository.currencyFlow
    private val exchangeRate = currency.flatMapLatest { getRateFlowCase.execute(it) }
    private val amount = MutableStateFlow(0f)
    val fiatAmount = formattedRate(exchangeRate, amount, TOKEN_TON)

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

}