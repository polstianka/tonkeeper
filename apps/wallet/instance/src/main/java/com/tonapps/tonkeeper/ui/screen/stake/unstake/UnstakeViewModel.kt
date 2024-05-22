package com.tonapps.tonkeeper.ui.screen.stake.unstake

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.Coin
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.api.jetton.JettonRepository
import com.tonapps.tonkeeper.api.totalFees
import com.tonapps.tonkeeper.extensions.emulate
import com.tonapps.tonkeeper.ui.component.keyvalue.KeyValueModel
import com.tonapps.tonkeeper.ui.screen.stake.confirm.ConfirmationArgs
import com.tonapps.tonkeeper.ui.screen.stake.model.icon
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.API
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ton.block.Coins
import org.ton.block.MsgAddressInt
import ton.Stake
import ton.TransactionHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UnstakeViewModel(
    private val ratesRepository: RatesRepository,
    private val walletManager: WalletManager,
    private val tokenRepository: TokenRepository,
    private val repository: StakeRepository,
    private val settingsRepository: SettingsRepository,
    private val jettonRepository: JettonRepository,
    private val api: API,
    private val resourceManager: ResourceManager,
) : ViewModel() {

    private val sdf = SimpleDateFormat("hh:mm:ss", Locale.getDefault())

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

            runTimer(pool.cycleEnd)

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

    private fun runTimer(end: Long) {
        flow<Unit> {
            while (true) {
                val current = System.currentTimeMillis()
                val date = Date(end - current)
                _uiState.update { it.copy(timerValue = sdf.format(date)) }
                delay(1000)
            }
        }.flowOn(Dispatchers.Default).launchIn(viewModelScope)
    }

    private fun selectToken(tokenAddress: String) {
        _uiState.update {
            it.copy(
                selectedTokenAddress = tokenAddress,
                canContinue = false,
            )
        }
    }

    private fun selectToken(token: AccountTokenEntity) {
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
            val queryId = TransactionHelper.getWalletQueryId()
            val amount = uiState.value.amount
            val currency = settingsRepository.currency
            val poolAddress = uiState.value.poolAddress
            val poolInfo = repository.get().pools.first { it.address == poolAddress }
            val wallet = walletManager.getWalletInfo() ?: error("No wallet info")
            val fee = 0.02f
            val body = Stake.unstakeLiquidTf(
                queryId,
                Coins.ofNano(Coin.toNano(amount - fee)),
                wallet.contract.address
            )
            val dest = uiState.value.selectedToken?.balance?.walletAddress.orEmpty()

            val stateInit = SeqnoHelper.getStateInitIfNeed(wallet, api)
            val gift = TransactionHelper.buildWalletTransfer(
                destination = MsgAddressInt.parse(dest),
                stateInit = stateInit,
                body = body,
                coins = Coins.ofNano(Coin.toNano(amount, decimals))
            )
            val emulated = wallet.emulate(api, gift)
            val feeInTon = emulated.totalFees
            val amountFee = Coin.toCoins(feeInTon)
            _uiState.update {
                val rates = ratesRepository.getRates(currency, it.selectedTokenAddress)
                val feeInCurrency = rates.convert(it.selectedTokenAddress, amountFee)
                val amountInCurrency = rates.convert(it.selectedTokenAddress, amount - fee)
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
                        key = resourceManager.getString(com.tonapps.wallet.localization.R.string.fee),
                        value = resourceManager.getString(
                            com.tonapps.wallet.localization.R.string.fee_placeholder,
                            CurrencyFormatter.format(currentTokenCode, amountFee)
                        ),
                        position = ListCell.Position.LAST,
                        caption = "\$$feeInCurrency"
                    ),
                )
                it.copy(
                    confirmScreenArgs = ConfirmationArgs(
                        amount = CurrencyFormatter.format(currentTokenCode, amount - fee)
                            .toString(),
                        amountInCurrency = CurrencyFormatter.format(currency.code, amountInCurrency)
                            .toString(),
                        imageRes = poolInfo.implementation.icon,
                        details = args,
                        walletTransfer = gift,
                        unstake = true
                    )
                )
            }
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
    val timerValue: String? = "",
    val poolAddress: String = ""
) {
    val selectedToken: AccountTokenEntity?
        get() = tokens.firstOrNull { it.address == selectedTokenAddress }

    val selectedTokenCode: String
        get() = selectedToken?.symbol ?: "TON"
}