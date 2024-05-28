package com.tonapps.tonkeeper.ui.screen.stake.amount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.Coin
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.api.totalFees
import com.tonapps.tonkeeper.extensions.emulate
import com.tonapps.tonkeeper.helper.NumberFormatter
import com.tonapps.tonkeeper.ui.component.keyvalue.KeyValueModel
import com.tonapps.tonkeeper.ui.screen.stake.confirm.ConfirmationArgs
import com.tonapps.tonkeeper.ui.screen.stake.model.icon
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.StakePoolsEntity
import com.tonapps.wallet.api.entity.StakePoolsEntity.PoolImplementationType
import com.tonapps.wallet.data.account.SeqnoHelper
import com.tonapps.wallet.data.account.legacy.WalletManager
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.stake.StakeRepository
import com.tonapps.wallet.data.token.TokenRepository
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import core.ResourceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ton.block.Coins
import org.ton.block.MsgAddressInt
import ton.Stake
import ton.TransactionHelper
import java.math.BigDecimal

class StakeAmountViewModel(
    private val repository: StakeRepository,
    private val settingsRepository: SettingsRepository,
    private val walletManager: WalletManager,
    private val ratesRepository: RatesRepository,
    private val tokenRepository: TokenRepository,
    private val api: API,
    private val resourceManager: ResourceManager
) : ViewModel() {

    private val currentToken: AccountTokenEntity?
        get() = uiState.value.selectedToken

    val currentBalance: Float
        get() {
            val poolType = _uiState.value.selectedPool?.pool?.implementation
            if (poolType == PoolImplementationType.liquidTF) {
                return ((currentToken?.balance?.value ?: 0f) - LIQUID_TF_FEE).coerceAtLeast(0f)
            }
            return currentToken?.balance?.value ?: 0f
        }

    val decimals: Int
        get() = currentToken?.decimals ?: 9

    private val currentTokenCode: String
        get() = uiState.value.selectedTokenCode

    private val _uiState = MutableStateFlow(StakeAmountUiState())
    val uiState: StateFlow<StakeAmountUiState> = _uiState

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
                it.copy(selectedPool = StakeAmountUiState.PoolInfo(pool, isMaxApy))
            }
            setValue(0f)
            if (address.isEmpty()) {
                repository.select(maxApy.address)
            }
        }.flowOn(Dispatchers.IO).launchIn(viewModelScope)
    }

    fun setAddress(address: String?) {
        address?.let {
            repository.select(it)
        }
    }

    fun selectToken(tokenAddress: String) {
        _uiState.update {
            it.copy(
                selectedTokenAddress = tokenAddress,
                canContinue = false,
                confirmScreenArgs = null
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

    fun onContinue() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            val queryId = TransactionHelper.getWalletQueryId()
            val poolAddress = repository.selectedPoolAddress.value
            val poolInfo = repository.get().pools.first { it.address == poolAddress }

            val body = when (poolInfo.implementation) {
                PoolImplementationType.whales -> Stake.stakeDepositWhales(queryId)
                PoolImplementationType.tf -> Stake.stakeDepositTf()
                PoolImplementationType.liquidTF -> Stake.stakeDepositLiquidTf(queryId)
            }

            val amount = uiState.value.amount
            val total = when (poolInfo.implementation) {
                PoolImplementationType.whales -> amount
                PoolImplementationType.tf -> amount
                PoolImplementationType.liquidTF -> amount + 1f
            }

            val wallet = walletManager.getWalletInfo() ?: error("No wallet info")
            val gift = TransactionHelper.buildWalletTransfer(
                destination = MsgAddressInt.parse(poolAddress),
                stateInit = SeqnoHelper.getStateInitIfNeed(wallet, api),
                body = body,
                coins = Coins.ofNano(Coin.toNano(total, decimals))
            )
            val emulated = wallet.emulate(api, gift)
            val feeInTon = emulated.totalFees
            val amountFee = Coin.toCoins(feeInTon)

            _uiState.update {
                val currentTokenAddress = _uiState.value.selectedTokenAddress
                val currency = _uiState.value.currency
                val rates = ratesRepository.getRates(currency, currentTokenAddress)
                val feeInCurrency = rates.convert(currentTokenAddress, amountFee)
                val args = listOf(
                    KeyValueModel.Simple(
                        key = resourceManager.getString(com.tonapps.wallet.localization.R.string.wallet),
                        value = wallet.name,
                        position = ListCell.Position.FIRST
                    ),
                    KeyValueModel.Simple(
                        key = resourceManager.getString(com.tonapps.wallet.localization.R.string.recipient),
                        value = poolInfo.name,
                        position = ListCell.Position.MIDDLE
                    ),
                    KeyValueModel.Simple(
                        key = resourceManager.getString(com.tonapps.wallet.localization.R.string.apy),
                        value = resourceManager.getString(
                            com.tonapps.wallet.localization.R.string.apy_short_percent_placeholder,
                            NumberFormatter.format(poolInfo.apy)
                        ),
                        position = ListCell.Position.MIDDLE
                    ),
                    KeyValueModel.Simple(
                        key = resourceManager.getString(com.tonapps.wallet.localization.R.string.fee),
                        value = resourceManager.getString(
                            com.tonapps.wallet.localization.R.string.fee_placeholder,
                            CurrencyFormatter.format(currentTokenCode, amountFee)
                        ),
                        position = ListCell.Position.LAST,
                        caption = "\$$feeInCurrency"
                    ),
                )
                val amountInCurrency = rates.convert(currentTokenAddress, amount)
                it.copy(
                    confirmScreenArgs = ConfirmationArgs(
                        amount = CurrencyFormatter.format(currentTokenCode, amount).toString(),
                        amountInCurrency = CurrencyFormatter.format(currency.code, amountInCurrency)
                            .toString(),
                        imageRes = poolInfo.implementation.icon,
                        details = args,
                        walletTransfer = gift,
                        unstake = false,
                    ),
                    loading = false
                )
            }
        }
    }

    private fun updateValue(newValue: Float) {
        val currentTokenAddress = _uiState.value.selectedTokenAddress
        val currency = _uiState.value.currency
        val rates = ratesRepository.getRates(currency, currentTokenAddress)
        val balanceInCurrency = rates.convert(currentTokenAddress, newValue)
        val min = Coin.toCoins(_uiState.value.selectedPool?.pool?.minStake ?: 0)
        val minFormatted = CurrencyFormatter.format(currentTokenCode, min).toString()

        val minWarning = if (newValue < min && newValue > 0f) minFormatted else ""
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
                canContinue = !insufficientBalance && currentBalance > 0 && newValue > 0 && minWarning.isEmpty(),
                maxActive = currentBalance == newValue,
                available = CurrencyFormatter.format(currentTokenCode, currentBalance),
                amount = newValue,
                minWarning = minWarning
            )
        }
    }

    companion object {
        private const val LIQUID_TF_FEE = 1.2f
    }
}

data class StakeAmountUiState(
    val pools: List<PoolInfo> = emptyList(),
    val maxApyByType: Map<PoolImplementationType, BigDecimal> = emptyMap(),
    val selectedPool: PoolInfo? = null,
    val amount: Float = 0f,
    val currency: WalletCurrency = WalletCurrency.TON,
    val available: CharSequence = "",
    val rate: CharSequence = "0 ",
    val insufficientBalance: Boolean = false,
    val remaining: CharSequence = "",
    val canContinue: Boolean = false,
    val maxActive: Boolean = false,
    val tokens: List<AccountTokenEntity> = emptyList(),
    val selectedTokenAddress: String = WalletCurrency.TON.code,
    val confirmScreenArgs: ConfirmationArgs? = null,
    val loading: Boolean = false,
    val minWarning: String = ""
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