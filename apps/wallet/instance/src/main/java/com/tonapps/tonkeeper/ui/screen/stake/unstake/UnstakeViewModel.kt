package com.tonapps.tonkeeper.ui.screen.stake.unstake

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.Coin
import com.tonapps.extensions.toByteArray
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.security.Security
import com.tonapps.security.hex
import com.tonapps.tonkeeper.api.jetton.JettonRepository
import com.tonapps.tonkeeper.api.totalFees
import com.tonapps.tonkeeper.extensions.emulate
import com.tonapps.tonkeeper.ui.screen.stake.confirm.ConfirmationArgs
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.StakePoolsEntity
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
import java.math.BigInteger

class UnstakeViewModel(
    private val ratesRepository: RatesRepository,
    private val walletManager: WalletManager,
    private val tokenRepository: TokenRepository,
    private val repository: StakeRepository,
    private val settingsRepository: SettingsRepository,
    private val jettonRepository: JettonRepository,
    private val api: API,
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

    private val _uiState = MutableStateFlow(UnstakeAmountUiState())
    val uiState: StateFlow<UnstakeAmountUiState> = _uiState

    fun load(address: String?) {
        address ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val currency = settingsRepository.currency
            val rate = settingsRepository.currency.code
            val wallet = walletManager.getWalletInfo() ?: error("No wallet info")
            val accountId = wallet.accountId
            val tokens = tokenRepository.get(currency, accountId, wallet.testnet)

            val pools = repository.get().pools
            val pool = pools.first { it.address == address }
            val jetton = jettonRepository.getByAddress(
                wallet.accountId,
                pool.liquidJettonMaster.orEmpty(),
                wallet.testnet
            ) ?: error("Jetton not found")
            _uiState.update {
                it.copy(
                    rate = "0 $rate",
                    tokens = tokens,
                    currency = currency,
                    selectedTokenAddress = jetton.jetton.address,
                    poolAddress = pool.address
                )
            }
            uiState.value.selectedToken?.let { selectToken(it) }
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

    fun onContinue() {
        viewModelScope.launch {
            val queryId = getWalletQueryId()
            val poolAddress = uiState.value.poolAddress
            val poolInfo = repository.get().pools.first { it.address == poolAddress }
            val amount = uiState.value.amount

            val fee = getWithdrawalFee(poolInfo.implementation)
            val wallet = walletManager.getWalletInfo() ?: error("No wallet info")
            val body = Transfer.unstakeLiquidTf(
                queryId,
                Coins.ofNano(Coin.toNano(amount - 0.05f)),
                wallet.contract.address
            )
            val dest = uiState.value.selectedToken?.balance?.walletAddress.orEmpty()

            val stateInit = getStateInitIfNeed(wallet)
            lastSeqno = getSeqno(wallet)
            val gift = buildWalletTransfer(
                destination = MsgAddressInt.parse(dest),
                stateInit = getStateInitIfNeed(wallet),
                body = body,
                coins = Coins.ofNano(Coin.toNano(amount, decimals))
            )
            val emulated = wallet.emulate(api, gift)
            val feeInTon = emulated.totalFees
            val amountFee = Coin.toCoins(feeInTon)
        }
    }

    private fun getWithdrawalFee(type: StakePoolsEntity.PoolImplementationType): Float {
        if (type == StakePoolsEntity.PoolImplementationType.whales) return 0.2f
        return 1f
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
        builder.sendMode = SendMode.PAY_GAS_SEPARATELY.value
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
}

data class UnstakeAmountUiState(
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
    val disclaimerTimerValue: String? = "",
    val poolAddress: String = ""
) {
    val selectedToken: AccountTokenEntity?
        get() = tokens.firstOrNull { it.address == selectedTokenAddress }

    val selectedTokenCode: String
        get() = selectedToken?.symbol ?: "TON"
}