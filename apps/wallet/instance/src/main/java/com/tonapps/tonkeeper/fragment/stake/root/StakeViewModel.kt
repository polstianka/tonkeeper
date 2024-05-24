package com.tonapps.tonkeeper.fragment.stake.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.core.TextWrapper
import com.tonapps.tonkeeper.core.emit
import com.tonapps.tonkeeper.core.observeFlow
import com.tonapps.tonkeeper.extensions.formattedRate
import com.tonapps.tonkeeper.fragment.stake.domain.StakingRepository
import com.tonapps.tonkeeper.fragment.stake.domain.StakingTransactionType
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingService
import com.tonapps.tonkeeper.fragment.stake.pool_details.PoolDetailsFragmentResult
import com.tonapps.tonkeeper.fragment.stake.presentation.description
import com.tonapps.tonkeeper.fragment.stake.presentation.getIconUri
import com.tonapps.tonkeeper.fragment.trade.domain.GetRateFlowCase
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.TokenRepository
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class StakeViewModel(
    settingsRepository: SettingsRepository,
    getRateFlowCase: GetRateFlowCase,
    walletRepository: WalletRepository,
    tokenRepository: TokenRepository,
    private val stakingRepository: StakingRepository,
) : ViewModel() {

    companion object {
        const val TOKEN_TON = "TON"
    }

    private val args = MutableSharedFlow<StakeArgs>(replay = 1)
    private val _events = MutableSharedFlow<StakeEvent>()
    private val currency = settingsRepository.currencyFlow
    private val exchangeRate = currency.flatMapLatest { getRateFlowCase.execute(it) }
    private val amount = MutableStateFlow(BigDecimal.ZERO)
    private val activeWallet = walletRepository.activeWalletFlow
    private val balance = combine(activeWallet, currency) { wallet, currency ->
        tokenRepository.get(currency, wallet.accountId, wallet.testnet)
            .firstOrNull { it.isTon }
    }
        .filterNotNull()
    private val stakingServices = stakingRepository.stakingPools
        .filter { it.isNotEmpty() }
    private val pickedPool = MutableSharedFlow<StakingPool>(replay = 1)

    val events: Flow<StakeEvent>
        get() = _events
    val fiatAmount = formattedRate(exchangeRate, amount, TOKEN_TON)
    val labelTextColorAttribute = combine(balance, amount) { balance, amount ->
        val red = com.tonapps.uikit.color.R.attr.accentRedColor
        val normal = com.tonapps.uikit.color.R.attr.textSecondaryColor
        when {
            balance.balance.value < amount -> red
            else -> normal
        }
    }
    val labelText = combine(balance, amount) { balance, amount ->
        val balanceAmount = balance.balance.value
        when {
            amount > balanceAmount ->
                TextWrapper.StringResource(Localization.insufficient_balance)

            else -> balanceAmount.minus(amount)
                .let { CurrencyFormatter.format("TON", it) }
                .let { TextWrapper.StringResource(Localization.available_balance, it) }
        }
    }
    val isButtonActive = combine(balance, amount, pickedPool) { balance, amount, pool ->
        balance.balance.value >= amount && amount >= pool.minStake
    }
    val iconUri = pickedPool.map { it.serviceType.getIconUri() }
    val optionTitle = pickedPool.map { it.name }
    val optionSubtitle = pickedPool.map { it.description() }
    val isMaxApy = pickedPool.map { it.isMaxApy }
    val isMaxGlowing = combine(balance, amount) { balance, amount ->
        balance.balance.value.compareTo(amount) == 0
    }

    init {
        observeFlow(activeWallet) {
            stakingRepository.loadStakingPools(it.address, it.testnet)
        }
        combine(stakingServices, args) { services, args ->
            val pool = args.pool ?: services.maxApy()
            pickedPool.emit(pool)
        }.launchIn(viewModelScope)
    }

    fun onCloseClicked() {
        emit(_events, StakeEvent.NavigateBack)
    }

    fun onInfoClicked() {
        emit(_events, StakeEvent.ShowInfo)
    }

    fun onAmountChanged(amount: BigDecimal) {
        if (amount == this.amount.value) return
        if (ignoreNextUpdate) {
            ignoreNextUpdate = false
            return
        }
        this.amount.value = amount
    }

    private var ignoreNextUpdate = false
    fun onMaxClicked() = viewModelScope.launch {
        val balance = balance.first().balance.value
        ignoreNextUpdate = true
        amount.value = balance
        _events.emit(StakeEvent.SetInputValue(balance))
    }

    fun onDropdownClicked() = viewModelScope.launch {
        val items = stakingServices.first()
        val pickedValue = pickedPool.first()
        val currency = currency.first()
        emit(_events, StakeEvent.PickStakingOption(items, pickedValue, currency))
    }

    fun onPoolPicked(result: PoolDetailsFragmentResult) {
        emit(pickedPool, result.pickedPool)
    }

    fun onButtonClicked() = viewModelScope.launch {
        val pool = pickedPool.first()
        val amount = amount.value
        val balance = balance.first()
        val event = StakeEvent.NavigateToConfirmFragment(
            pool = pool,
            amount = amount,
            type = StakingTransactionType.DEPOSIT,
            isSendAll = balance.balance.value == amount
        )
        _events.emit(event)
    }

    fun provideArgs(stakeArgs: StakeArgs) {
        emit(this.args, stakeArgs)
    }
}


private fun List<StakingService>.maxApy() = first().pools.first()
