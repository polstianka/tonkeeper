package com.tonapps.tonkeeper.fragment.staking.withdrawal.finish

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.Coin
import com.tonapps.blockchain.ton.tlb.StakingTlb
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.fragment.send.TransactionData
import com.tonapps.tonkeeper.helper.flow.TransactionEmulator
import com.tonapps.tonkeeper.helper.flow.TransactionSender
import com.tonapps.tonkeeper.password.PasscodeRepository
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.BalanceStakeEntity
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.account.legacy.WalletManager
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.rates.entity.RatesEntity
import com.tonapps.wallet.data.settings.SettingsRepository
import io.tonapi.models.PoolImplementationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.ton.block.Coins
import org.ton.block.MsgAddressInt
import org.ton.contract.wallet.WalletTransfer
import org.ton.contract.wallet.WalletTransferBuilder
import java.math.BigInteger

class WithdrawalFinishScreenViewModel(
    private val _stake: BalanceStakeEntity,
    private val settingsRepository: SettingsRepository,
    private val walletRepository: WalletRepository,
    passcodeRepository: PasscodeRepository,
    private val walletManager: WalletManager,
    private val ratesRepository: RatesRepository,
    api: API
) : ViewModel() {

    private val _confirmScreenStateFlow = MutableStateFlow<ConfirmScreenState?>(null)
    private val confirmScreenStateFlow = _confirmScreenStateFlow.asStateFlow().filterNotNull()

    init {
        viewModelScope.launch {
            val wallet = walletRepository.getWallet(walletManager.getActiveWallet())!!
            _confirmScreenStateFlow.value = ConfirmScreenState(
                stake = _stake,
                walletEntity = wallet,
                rates = ratesRepository.cache(
                    settingsRepository.currency,
                    listOf(TokenEntity.TON.address)
                )
            )
        }
    }

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
        val stake: BalanceStakeEntity,
    ) : TransactionEmulator.Request {
        fun build(): StakingTlb.MessageData {
            if (stake.pool.implementation.type != PoolImplementationType.whales) {
                throw NotImplementedError("I don't know how to take stake from other pools")
            }

            return StakingTlb.buildUnstakeTxParams(
                queryId = TransactionData.getWalletQueryId(),
                poolType = stake.pool.implementation.type,
                poolAddress = MsgAddressInt.parse(stake.pool.address),
                amount = Coins.ofNano(stake.readyWithdrawNano),
                useAllAmount = false,
                responseAddress = MsgAddressInt.parse(walletEntity.address),
                stakingJettonWalletAddress = null
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

        val valueFmt =
            CurrencyFormatter.format(TokenEntity.TON.symbol, Coin.toCoins(stake.readyWithdrawNano))
        val valueCurrencyFmt = CurrencyFormatter.formatFiat(
            rates.currency.code,
            rates.convert(TokenEntity.TON.address, Coin.toCoins(stake.readyWithdrawNano))
        )

        fun getFeeFmt(fee: BigInteger) =
            "≈ " + CurrencyFormatter.format(TokenEntity.TON.symbol, Coin.toCoins(fee))

        fun getFeeInCurrencyFmt(fee: BigInteger) = "≈ " + CurrencyFormatter.formatFiat(
            rates.currency.code,
            rates.convert(TokenEntity.TON.address, Coin.toCoins(fee))
        )
    }
}