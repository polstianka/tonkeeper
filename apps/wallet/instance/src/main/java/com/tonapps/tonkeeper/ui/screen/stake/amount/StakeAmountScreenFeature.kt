package com.tonapps.tonkeeper.ui.screen.stake.amount

import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.Coin
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.ui.screen.stake.StakeData
import com.tonapps.tonkeeper.ui.screen.stake.StakingRepository
import com.tonapps.wallet.api.Tonapi
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.TokenRepository
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import core.QueueScope
import io.tonapi.models.PoolImplementationType
import io.tonapi.models.PoolInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uikit.mvi.UiFeature

class StakeAmountScreenFeature(
    private val tokenRepository: TokenRepository,
    private val ratesRepository: RatesRepository,
    private val stakingRepository: StakingRepository,
    private val settingsRepository: SettingsRepository
) : UiFeature<StakeAmountScreenState, StakeAmountScreenEffect>(StakeAmountScreenState()) {

    private val currency: WalletCurrency
        get() = settingsRepository.currency

    private val isUnstake: Boolean
        get() = uiState.value.isUnstake

    private val currentToken: AccountTokenEntity?
        get() = uiState.value.selectedToken

    private val stakingToken: AccountTokenEntity?
        get() = uiState.value.stakingToken

    private val currentTokenCode: String
        get() = uiState.value.selectedTokenCode

    val currentBalance: Float
        get() = if (isUnstake) { stakingToken?.balance?.value?.toFloat() ?: 0f } else { currentToken?.balance?.value?.toFloat() ?: 0f }

    val decimals: Int
        get() = currentToken?.decimals ?: 9

    private val queueScope = QueueScope(Dispatchers.IO)

    /*init {
        viewModelScope.launch {
            updateUiState { currentState ->
                currentState.copy(
                    loading = true
                )
            }
            loadData()
        }
    }*/

    fun setData(data: StakeData) {
        updateUiState { currentState ->
            currentState.copy(
                address = data.preAddress,
                isUnstake = data.preUnstake,
                loading = uiState.value.wallet == null
            )
        }
        if (uiState.value.wallet == null) {
            viewModelScope.launch {
                loadData()
            }
        }
    }

    private fun getCurrentTokenAddress(): String {
        return currentToken?.address ?: "TON"
    }

    private suspend fun loadData() = withContext(Dispatchers.IO) {
        val wallet = App.walletManager.getWalletInfo()!!
        val accountId = wallet.accountId
        val tokens = tokenRepository.get(currency, accountId, wallet.testnet)

        updateUiState {
            it.copy(
                wallet = wallet,
                tokens = tokens
            )
        }
        val stakingData = stakingRepository.getPools(wallet.testnet, accountId)
        val pools = stakingData?.pools ?: emptyList()
        val implMap = stakingData?.implementations ?: emptyMap()
        var selectedPool: PoolInfo? = null

        if (pools.isNotEmpty()) {
            var liquids = pools
                .filter { it.implementation.value == PoolImplementationType.liquidTF.value }
                .maxByOrNull { it.apy }
            if (liquids == null) {
                liquids = pools.maxByOrNull { it.apy }
            }

            selectedPool = liquids
        }

        updateUiState { state ->
            state.copy(
                selectedTokenAddress = WalletCurrency.TON.code,
                stakingPools = pools,
                selectedPool = selectedPool,
                implMap = implMap,
                loading = false,
                stakingToken = if (isUnstake) tokens.find { it.address == uiState.value.address } else null
            )
        }



        setValue(0f)
    }

    fun selectToken(tokenAddress: String) {
        updateUiState {
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

    fun setPool(poolInfo: PoolInfo?) {
        updateUiState {
            it.copy(
                selectedPool = poolInfo
            )
        }
    }

    private suspend fun updateValue(newValue: Float) {
        val wallet = App.walletManager.getWalletInfo() ?: return
        val accountId = wallet.accountId
        val currentTokenAddress = getCurrentTokenAddress()

        val rates = ratesRepository.getRates(currency, currentTokenAddress)
        val balanceInCurrency = rates.convert(currentTokenAddress, newValue)

        val insufficientBalance = newValue > currentBalance
        val remaining = if (newValue > 0) {
            val value = currentBalance - newValue
            CurrencyFormatter.format(currentTokenCode, value)
        } else {
            ""
        }

        val min = Coin.toCoins(uiState.value.selectedPool?.minStake ?: 0)

        updateUiState { currentState ->
            currentState.copy(
                rate = "${CurrencyFormatter.formatFiat("", balanceInCurrency)} ${currency.code}",
                insufficientBalance = insufficientBalance,
                remaining = remaining,
                canContinue = !insufficientBalance && currentBalance > 0 && newValue >= min,
                maxActive = currentBalance == newValue,
                available = CurrencyFormatter.format(currentTokenCode, currentBalance)
            )
        }
    }

    fun setValue(value: Float) {
        viewModelScope.launch {
            updateValue(value)
        }
    }

    override fun onCleared() {
        super.onCleared()
        queueScope.cancel()
    }
}