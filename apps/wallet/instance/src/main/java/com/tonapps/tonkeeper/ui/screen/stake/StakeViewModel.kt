package com.tonapps.tonkeeper.ui.screen.stake

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.wallet.api.entity.StakePoolsEntity
import com.tonapps.wallet.api.entity.StakePoolsEntity.PoolImplementationType
import com.tonapps.wallet.data.account.legacy.WalletLegacy
import com.tonapps.wallet.data.account.legacy.WalletManager
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.stake.StakeRepository
import com.tonapps.wallet.data.token.TokenRepository
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

class StakeViewModel(
    private val repository: StakeRepository,
    private val settingsRepository: SettingsRepository,
    private val walletManager: WalletManager,
    private val ratesRepository: RatesRepository,
    private val tokenRepository: TokenRepository,
) : ViewModel() {

    private val currentToken: AccountTokenEntity?
        get() = uiState.value.selectedToken

    val currentBalance: Float
        get() = currentToken?.balance?.value ?: 0f

    val decimals: Int
        get() = currentToken?.decimals ?: 9

    private val currentTokenCode: String
        get() = uiState.value.selectedTokenCode

    private val _uiState = MutableStateFlow(StakeUiState())
    val uiState: StateFlow<StakeUiState> = _uiState

    init {
        val currency = settingsRepository.currency
        val rate = settingsRepository.currency.code
        repository.selectedPoolAddress.onEach { address ->
            val wallet = walletManager.getWalletInfo() ?: error("No wallet info")
            val accountId = wallet.accountId
            val tokens = tokenRepository.get(currency, accountId, wallet.testnet)
            selectToken(tokens.first())
            _uiState.update {
                it.copy(
                    rate = "0 $rate",
                    wallet = wallet,
                    tokens = tokens,
                    currency = currency,
                    selectedTokenAddress = WalletCurrency.TON.code,
                )
            }
            val pools = repository.get().pools
            val maxApy = pools.maxByOrNull { it.apy } ?: error("No pools")
            val pool = if (address.isEmpty()) maxApy else pools.first { it.address == address }
            val isMaxApy = address.isEmpty() || pool.address == maxApy.address

            _uiState.update {
                it.copy(selectedPool = StakeUiState.PoolInfo(pool, isMaxApy))
            }
            setValue(0f)
            if (address.isEmpty()) {
                repository.select(maxApy.address)
            }
        }.flowOn(Dispatchers.IO).launchIn(viewModelScope)
    }

    fun selectToken(tokenAddress: String) {
        _uiState.update {
            it.copy(
                selectedTokenAddress = tokenAddress,
                canContinue = false,
            )
        }
    }

    fun selectToken(token: AccountTokenEntity) {
        selectToken(token.address)

        viewModelScope.launch {
            updateValue(uiState.value.amount)
        }
    }

    fun setValue(value: Float) {
        _uiState.update { currentState ->
            currentState.copy(canContinue = false)
        }

        viewModelScope.launch {
            updateValue(value)
        }
    }

    private fun updateValue(newValue: Float) {
        val currentTokenAddress = _uiState.value.selectedTokenAddress
        val currency = _uiState.value.currency
        val rates = ratesRepository.getRates(currency, currentTokenAddress)
        val balanceInCurrency = rates.convert(currentTokenAddress, newValue)

        val insufficientBalance = newValue > currentBalance
        val remaining = if (newValue > 0) {
            val value = currentBalance - newValue
            CurrencyFormatter.format(currentTokenCode, value)
        } else {
            ""
        }

        _uiState.update { currentState ->
            currentState.copy(
                rate = CurrencyFormatter.formatFiat(currency.code, balanceInCurrency),
                insufficientBalance = insufficientBalance,
                remaining = remaining,
                canContinue = !insufficientBalance && currentBalance > 0 && newValue > 0,
                maxActive = currentBalance == newValue,
                available = CurrencyFormatter.format(currentTokenCode, currentBalance)
            )
        }
    }
}

data class StakeUiState(
    val implementations: List<PoolImplementationType> = listOf(
        PoolImplementationType.liquidTF,
        PoolImplementationType.whales,
        PoolImplementationType.tf
    ),
    val pools: List<PoolInfo> = emptyList(),
    val maxApyByType: Map<PoolImplementationType, BigDecimal> = emptyMap(),
    val selectedPool: PoolInfo? = null,

    val wallet: WalletLegacy? = null,
    val amount: Float = 0f,
    val currency: WalletCurrency = WalletCurrency.TON,
    val available: CharSequence = "",
    val rate: CharSequence = "0 ",
    val insufficientBalance: Boolean = false,
    val remaining: CharSequence = "",
    val canContinue: Boolean = false,
    val maxActive: Boolean = false,
    val tokens: List<AccountTokenEntity> = emptyList(),
    val selectedTokenAddress: String = WalletCurrency.TON.code
) {
    data class PoolInfo(
        val pool: StakePoolsEntity.PoolInfo,
        val isMaxApy: Boolean,
    )

    val selectedToken: AccountTokenEntity?
        get() = tokens.firstOrNull { it.address == selectedTokenAddress }

    val selectedTokenCode: String
        get() = selectedToken?.symbol ?: "TON"
}