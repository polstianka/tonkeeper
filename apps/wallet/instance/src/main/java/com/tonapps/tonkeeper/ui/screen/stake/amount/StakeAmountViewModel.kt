package com.tonapps.tonkeeper.ui.screen.stake.amount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.Coin
import com.tonapps.extensions.toByteArray
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.security.Security
import com.tonapps.security.hex
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
import com.tonapps.wallet.data.account.legacy.WalletLegacy
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
import org.ton.block.StateInit
import org.ton.cell.Cell
import org.ton.contract.wallet.WalletTransfer
import org.ton.contract.wallet.WalletTransferBuilder
import ton.SendMode
import ton.transfer.Transfer
import java.math.BigDecimal
import java.math.BigInteger

class StakeViewModel(
    private val repository: StakeRepository,
    private val settingsRepository: SettingsRepository,
    private val walletManager: WalletManager,
    private val ratesRepository: RatesRepository,
    private val tokenRepository: TokenRepository,
    private val api: API,
    private val resourceManager: ResourceManager
) : ViewModel() {

    private var lastSeqno = -1

    private val currentToken: AccountTokenEntity?
        get() = uiState.value.selectedToken

    val currentBalance: Float
        get() = currentToken?.balance?.value ?: 0f

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
            val queryId = getWalletQueryId()
            val poolAddress = repository.selectedPoolAddress.value
            val poolInfo = repository.get().pools.first { it.address == poolAddress }

            val body = when (poolInfo.implementation) {
                PoolImplementationType.whales -> Transfer.stakeDepositWhales(queryId)
                PoolImplementationType.tf -> Transfer.stakeDepositTf()
                PoolImplementationType.liquidTF -> Transfer.stakeDepositLiquidTf(queryId)
            }

            val amount = uiState.value.amount
            val total = when (poolInfo.implementation) {
                PoolImplementationType.whales -> amount
                PoolImplementationType.tf -> amount
                PoolImplementationType.liquidTF -> {
                    val isAll = amount == currentBalance
                    if (isAll) amount - 1.2f else amount + 1f
                }
            }
            val wallet = walletManager.getWalletInfo() ?: error("No wallet info")
            val stateInit = getStateInitIfNeed(wallet)
            lastSeqno = getSeqno(wallet)
            val gift = buildWalletTransfer(
                destination = MsgAddressInt.parse(poolAddress),
                stateInit = getStateInitIfNeed(wallet),
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
                val amountInCurrency = rates.convert(currentTokenAddress, total)
                it.copy(
                    confirmScreenArgs = ConfirmationArgs(
                        amount = CurrencyFormatter.format(currentTokenCode, total).toString(),
                        amountInCurrency = CurrencyFormatter.format(currency.code, amountInCurrency)
                            .toString(),
                        imageRes = poolInfo.implementation.icon,
                        details = args,
                        walletTransfer = gift
                    )
                )
            }
        }
    }

    private suspend fun getStateInitIfNeed(wallet: WalletLegacy): StateInit? {
        if (lastSeqno == -1) {
            lastSeqno = getSeqno(wallet)
        }
        if (lastSeqno == 0) {
            return wallet.contract.stateInit
        }
        return null
    }

    private suspend fun getSeqno(wallet: WalletLegacy): Int {
        if (lastSeqno == 0) {
            lastSeqno = wallet.getSeqno(api)
        }
        return lastSeqno
    }

    private suspend fun WalletLegacy.getSeqno(api: API): Int {
        return try {
            api.getAccountSeqno(accountId, testnet)
        } catch (e: Throwable) {
            0
        }
    }

    private fun buildWalletTransfer(
        destination: MsgAddressInt,
        stateInit: StateInit?,
        body: Cell,
        coins: Coins
    ): WalletTransfer {
        val builder = WalletTransferBuilder()
        builder.bounceable = true
        builder.destination = destination
        builder.body = body
        builder.sendMode = SendMode.PAY_GAS_SEPARATELY.value + SendMode.IGNORE_ERRORS.value
        builder.coins = coins
        builder.stateInit = stateInit
        return builder.build()
    }

    private fun getWalletQueryId(): BigInteger {
        try {
            val tonkeeperSignature = 0x546de4ef.toByteArray()
            val randomBytes = Security.randomBytes(4)
            val value = tonkeeperSignature + randomBytes
            val hexString = hex(value)
            return BigInteger(hexString, 16)
        } catch (e: Throwable) {
            return BigInteger.ZERO
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
                available = CurrencyFormatter.format(currentTokenCode, currentBalance),
                amount = newValue
            )
        }
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