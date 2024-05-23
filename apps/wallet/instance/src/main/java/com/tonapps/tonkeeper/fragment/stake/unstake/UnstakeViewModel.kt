package com.tonapps.tonkeeper.fragment.stake.unstake

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.core.TextWrapper
import com.tonapps.tonkeeper.core.emit
import com.tonapps.tonkeeper.fragment.stake.domain.model.getCryptoBalance
import com.tonapps.tonkeeper.fragment.stake.domain.model.getFiatBalance
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import com.tonapps.wallet.localization.R as LocalizationR

@OptIn(ExperimentalCoroutinesApi::class)
class UnstakeViewModel : ViewModel() {
    private val args = MutableSharedFlow<UnstakeArgs>(replay = 1)
    private val _events = MutableSharedFlow<UnstakeEvent>()
    private val amount = MutableStateFlow(BigDecimal.ZERO)

    val events: Flow<UnstakeEvent>
        get() = _events
    val description = args.flatMapLatest { args ->
        val pool = args.balance.pool
        val cycleEnd = pool.cycleEnd
        val endTimestamp = cycleEnd * 1000L
        flow {
            while (true) {
                val now = System.currentTimeMillis()
                if (now > endTimestamp) {
                    emit(TextWrapper.StringResource(LocalizationR.string.no_time))
                    break
                } else {
                    val duration = formatIntervalDuration(endTimestamp - now)
                    emit(TextWrapper.StringResource(LocalizationR.string.unstake_description, duration))
                    delay(1000L)
                }
            }
        }
    }
    val available = combine(args, amount) { args, amount ->
        val balance = calculateBalance(args)
        balance - amount
    }.shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)
    val fiatText = combine(args, amount) { args, amount ->
        val fiatAmount = args.balance.getFiatBalance()
        CurrencyFormatter.format(args.balance.fiatCurrency.code, fiatAmount)
    }
    val isButtonEnabled = combine(amount, available) { amount, available ->
        amount > BigDecimal.ZERO && available >= BigDecimal.ZERO
    }
    val isMax = combine(args, amount) { args, amount ->
        val balance = calculateBalance(args)
        balance == amount
    }.shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)


    private fun formatIntervalDuration(l: Long): String {
        val seconds = l / 1000L
        val minutes = (seconds / 60L) % 60L
        val hours = seconds / 3600
        val secondsLeft = seconds % 60L
        return String.format("%02d:%02d:%02d", hours, minutes, secondsLeft)
    }

    private fun calculateBalance(args: UnstakeArgs): BigDecimal {
        return args.balance.getCryptoBalance()
    }

    fun provideArgs(args: UnstakeArgs) {
        emit(this.args, args)
    }

    fun onCloseClicked() {
        emit(_events, UnstakeEvent.NavigateBack)
    }

    fun onMaxClicked() = viewModelScope.launch {
        val balance = calculateBalance(args.first())
        amount.value = balance
        ignoreNextUpdate = true
        val inputText = CurrencyFormatter.format(balance, 2)
        _events.emit(UnstakeEvent.FillInput(inputText))
    }

    private var ignoreNextUpdate = false
    fun onAmountChanged(amount: BigDecimal) {
        if (this.amount.value == amount) return
        if (ignoreNextUpdate) {
            ignoreNextUpdate = false
            return
        }
        this.amount.value = amount
    }

    fun onButtonClicked() = viewModelScope.launch {
        val args = args.first()
        val amount = amount.value
        val event = UnstakeEvent.NavigateToConfirmStake(
            args.balance.pool,
            amount,
            isSendAll = isMax.first()
        )
        emit(_events, event)
    }
}