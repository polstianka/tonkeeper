package com.tonapps.tonkeeper.fragment.staking.withdrawal

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.Coin
import com.tonapps.blockchain.ton.tlb.StakingTlb
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.fragment.send.TransactionData
import com.tonapps.tonkeeper.helper.Coin2
import com.tonapps.tonkeeper.helper.flow.AmountInputController
import com.tonapps.tonkeeper.helper.flow.CountdownTimer
import com.tonapps.tonkeeper.helper.flow.TransactionEmulator
import com.tonapps.tonkeeper.helper.flow.TransactionSender
import com.tonapps.tonkeeper.password.PasscodeRepository
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.BalanceEntity
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.account.legacy.WalletManager
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.rates.entity.RatesEntity
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.ton.block.Coins
import org.ton.block.MsgAddressInt
import org.ton.contract.wallet.WalletTransfer
import org.ton.contract.wallet.WalletTransferBuilder
import uikit.extensions.collectFlow
import java.math.BigDecimal
import java.math.BigInteger

class WithdrawalScreenViewModel(
    private val settingsRepository: SettingsRepository,
    walletRepository: WalletRepository,
    passcodeRepository: PasscodeRepository,
    walletManager: WalletManager,
    private val ratesRepository: RatesRepository,
    api: API
) : ViewModel() {
    fun setEntity(stake: AccountTokenEntity) {
        _stakeFlow.value = stake
    }

    private val _stakeFlow = MutableStateFlow<AccountTokenEntity?>(null)


    /* Amount Input */

    val inputAmountController = AmountInputController()

    private val _amountScreenStateFlow = MutableStateFlow<AmountInputState?>(null)
    val amountScreenStateFlow = _amountScreenStateFlow.asStateFlow().filterNotNull()

    val countdownFlow =
        CountdownTimer.create(_stakeFlow.filterNotNull().map { it.balance.stake!!.pool.cycleEnd })

    data class AmountInputState(
        val input: AmountInputController.State,
        val wallet: WalletEntity,
        val rates: RatesEntity,
        val stake: BalanceEntity
    ) {
        val fiatFmt
            get() = CurrencyFormatter.format(
                rates.currency.code,
                rates.convert(
                    TokenEntity.TON.address,
                    input.input.coin?.toFloat(input.inputState.decimals) ?: 0f
                )
            )
    }

    fun openConfirmPage() {
        _stakeFlow.value?.let { entity ->
            _amountScreenStateFlow.value?.let { state ->
                _confirmScreenStateFlow.value = ConfirmScreenState(
                    walletEntity = state.wallet,
                    amount = state.input.input.coin!!,
                    rates = ratesRepository.cache(
                        settingsRepository.currency,
                        listOf(TokenEntity.TON.address)
                    ),
                    balance = state.stake,
                    jettonWalletAddress = if (!entity.isTon) entity.balance.walletAddress else null,
                    useAllAmount = state.input.useMaxAmount
                )
                _pageStateFlow.value = _pageStateFlow.value.copy(confirmVisibility = true)
            }
        }
    }


    /* Confirm Input */

    private val _confirmScreenStateFlow = MutableStateFlow<ConfirmScreenState?>(null)
    private val confirmScreenStateFlow = _confirmScreenStateFlow.asStateFlow().filterNotNull()

    val transactionEmulationFlow =
        TransactionEmulator.makeTransactionEmulatorFlow(api, confirmScreenStateFlow)
    val transactionSender = TransactionSender(api, passcodeRepository, walletManager, viewModelScope)

    fun send(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionSender.send(context, _confirmScreenStateFlow.value!!)
        }
    }

    data class ConfirmScreenState(
        val walletEntity: WalletEntity,
        val rates: RatesEntity,
        val amount: Coin2,
        val balance: BalanceEntity,
        val jettonWalletAddress: String?,
        val useAllAmount: Boolean
    ) : TransactionEmulator.Request {
        val stake = balance.stake!!

        fun build(): StakingTlb.MessageData {
            val totalAmount = stake.pool.liquidJettonMaster?.let { balance.nano } ?: stake.amountNano
            var value = if (useAllAmount) {
                totalAmount
            } else {
                (BigDecimal(amount.value) / BigDecimal.valueOf(stake.tonRate.toDouble())).toBigInteger()
            }

            if (value > totalAmount) {
                value = totalAmount
            }

            return StakingTlb.buildUnstakeTxParams(
                queryId = TransactionData.getWalletQueryId(),
                poolType = stake.pool.implementation.type,
                poolAddress = MsgAddressInt.parse(stake.pool.address),
                amount = Coins.ofNano(value),
                useAllAmount = useAllAmount,
                responseAddress = MsgAddressInt.parse(walletEntity.address),
                stakingJettonWalletAddress = jettonWalletAddress?.let { MsgAddressInt.parse(it) }
            )
        }

        override fun getWallet(): WalletEntity = walletEntity

        override fun getTransfer(): WalletTransfer {
            val data = build()
            val builder = WalletTransferBuilder()
            builder.bounceable = true
            builder.destination = data.to
            builder.body = data.payload
            builder.sendMode = 3
            builder.coins = data.gasAmount
            return builder.build()
        }

        val valueFmt = CurrencyFormatter.format("TON", Coin.toCoins(amount.value))
        val valueCurrencyFmt = CurrencyFormatter.formatFiat(
            rates.currency.code,
            rates.convert(TokenEntity.TON.address, Coin.toCoins(amount.value))
        )

        fun getFeeFmt(fee: BigInteger) = "≈ " + CurrencyFormatter.format("TON", Coin.toCoins(fee))

        fun getFeeInCurrencyFmt(fee: BigInteger) = "≈ " + CurrencyFormatter.formatFiat(
            rates.currency.code,
            rates.convert(TokenEntity.TON.address, Coin.toCoins(fee))
        )
    }


    /* Pager */

    data class PagerState(
        val confirmVisibility: Boolean,
    )

    private val _pageStateFlow = MutableStateFlow(PagerState(confirmVisibility = false))
    val pageStateFlow = _pageStateFlow.asStateFlow()


    fun prevPage() {
        _pageStateFlow.value = _pageStateFlow.value.copy(confirmVisibility = false)
    }


    init {
        collectFlow(_stakeFlow.filterNotNull()) { stake ->
            inputAmountController.setInputParams(
                BigInteger.ZERO,
                stake.balance.stake!!.amountNano,
                TokenEntity.TON.decimals,
                TokenEntity.TON.symbol
            )
        }

        combine(
            walletRepository.activeWalletFlow,
            _stakeFlow.filterNotNull(),
            inputAmountController.outputStateFlow
        ) { wallet, stake, input ->
            _amountScreenStateFlow.value = AmountInputState(
                input = input,
                wallet = wallet,
                rates = ratesRepository.cache(
                    settingsRepository.currency,
                    listOf(TokenEntity.TON.address)
                ),
                stake = stake.balance
            )
        }.launchIn(viewModelScope)
    }
}