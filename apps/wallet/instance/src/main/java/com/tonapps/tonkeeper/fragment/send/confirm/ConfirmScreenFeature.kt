package com.tonapps.tonkeeper.fragment.send.confirm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.Coin
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.api.totalFees
import com.tonapps.tonkeeper.core.history.HistoryHelper
import com.tonapps.tonkeeper.event.WalletStateUpdateEvent
import com.tonapps.tonkeeper.extensions.emulate
import com.tonapps.tonkeeper.extensions.getSeqno
import com.tonapps.tonkeeper.extensions.label
import com.tonapps.tonkeeper.extensions.sendToBlockchain
import com.tonapps.tonkeeper.fragment.send.TransactionData
import com.tonapps.tonkeeper.password.PasscodeRepository
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.legacy.WalletLegacy
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.RatesRepository
import core.EventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ton.bitstring.BitString
import org.ton.block.StateInit
import org.ton.cell.Cell
import uikit.mvi.UiFeature
import uikit.widget.ProcessTaskView

@Deprecated("Need refactoring")
class ConfirmScreenFeature(
    private val passcodeRepository: PasscodeRepository,
    private val ratesRepository: RatesRepository,
    private val api: API,
    private val historyHelper: HistoryHelper,
): UiFeature<ConfirmScreenState, ConfirmScreenEffect>(ConfirmScreenState()) {

    private val currency: WalletCurrency
        get() = App.settings.currency

    private var lastSeqno = -1
    private var lastUnsignedBody: Cell? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val wallet = App.walletManager.getWalletInfo() ?: return@launch
            updateUiState {
                it.copy(
                    signer = wallet.signer,
                    walletLabel = wallet.label
                )
            }
        }
    }

    private suspend fun buildUnsignedBody(
        wallet: WalletLegacy,
        seqno: Int,
        tx: TransactionData
    ): Cell {
        val stateInit = getStateInitIfNeed(wallet)
        val transfer = tx.buildWalletTransfer(wallet.contract.address, stateInit)
        return wallet.contract.createTransferUnsignedBody(seqno = seqno, gifts = arrayOf(transfer))
    }

    fun sendSignature(data: ByteArray) {
        updateUiState {
            it.copy(
                processActive = true,
                processState = ProcessTaskView.State.LOADING
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val wallet = App.walletManager.getWalletInfo() ?: throw Exception("failed to get wallet")
                val contract = wallet.contract

                val unsignedBody = lastUnsignedBody ?: throw Exception("unsigned body is null")
                val signature = BitString(data)
                val signerBody = contract.signedBody(signature, unsignedBody)
                val b = contract.createTransferMessageCell(wallet.contract.address, lastSeqno, signerBody)
                if (!wallet.sendToBlockchain(api, b)) {
                    failedResult()
                    return@launch
                }
                successResult()
            } catch (e: Throwable) {
                failedResult()
            }
        }
    }

    fun sign(tx: TransactionData) {
        updateUiState {
            it.copy(
                processActive = true,
                processState = ProcessTaskView.State.LOADING
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            val wallet = App.walletManager.getWalletInfo() ?: throw Exception("failed to get wallet")
            lastSeqno = getSeqno(wallet)
            lastUnsignedBody = buildUnsignedBody(wallet, lastSeqno, tx)

            sendEffect(ConfirmScreenEffect.OpenSignerApp(lastUnsignedBody!!, wallet.publicKey))
        }
    }

    private suspend fun getStateInitIfNeed(wallet: WalletLegacy): StateInit? {
        if (lastSeqno == -1) {
            lastSeqno = getSeqno(wallet)
        }
        /*if (lastSeqno == 0) {
            return wallet.contract.stateInit
        }*/
        return null
    }

    fun send(context: Context, tx: TransactionData) {
        updateUiState {
            it.copy(
                processActive = true,
                processState = ProcessTaskView.State.LOADING
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val wallet = App.walletManager.getWalletInfo() ?: throw Exception("failed to get wallet")
                if (!passcodeRepository.confirmation(context)) {
                    throw Exception("failed to request passcode")
                }
                val privateKey = App.walletManager.getPrivateKey(wallet.id)
                val gift = tx.buildWalletTransfer(wallet.contract.address, getStateInitIfNeed(wallet))
                wallet.sendToBlockchain(api, privateKey, gift) ?: throw Exception("failed to send to blockchain")

                successResult()
            } catch (e: Throwable) {
                failedResult()
            }
        }
    }

    fun setFailedResult() {
        viewModelScope.launch {
            failedResult()
        }
    }

    private suspend fun failedResult() {
        updateUiState {
            it.copy(
                processState = ProcessTaskView.State.FAILED
            )
        }

        delay(5000)
        sendEffect(ConfirmScreenEffect.CloseScreen(false))
    }

    private suspend fun successResult() {
        updateUiState {
            it.copy(
                processState = ProcessTaskView.State.SUCCESS
            )
        }

        val emulatedEventItems = uiState.value.emulatedEventItems
        if (emulatedEventItems.isNotEmpty()) {
            EventBus.post(WalletStateUpdateEvent)
        }

        delay(1000)
        sendEffect(ConfirmScreenEffect.CloseScreen(true))
    }

    fun setAmount(amountRaw: String, decimals: Int, tokenAddress: String, symbol: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val value = Coin.prepareValue(amountRaw).toFloatOrNull() ?: 0f
            val rates = ratesRepository.getRates(currency, tokenAddress)
            val fiat = rates.convert(tokenAddress, value)

            updateUiState {
                it.copy(
                    amount = CurrencyFormatter.format(symbol, value),
                    amountInCurrency = "≈ " + CurrencyFormatter.format(currency.code, fiat)
                )
            }
        }
    }

    fun requestFee(tx: TransactionData) {
        updateUiState {
            it.copy(
                fee = "",
                feeInCurrency = "",
                buttonEnabled = false
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            val wallet = App.walletManager.getWalletInfo() ?: return@launch
            try {
                lastSeqno = getSeqno(wallet)
                val gift = tx.buildWalletTransfer(wallet.contract.address, getStateInitIfNeed(wallet))
                val emulate = wallet.emulate(api, gift)
                val feeInTon = emulate.totalFees
                val actions = historyHelper.mapping(wallet, emulate.event, false)
                val tokenAddress = tx.tokenAddress

                val rates = ratesRepository.getRates(currency, tokenAddress)
                val feeInCurrency = rates.convert(tokenAddress, Coin.toCoins(feeInTon))

                val amount = Coin.toCoins(feeInTon)
                updateUiState {
                    it.copy(
                        feeValue = feeInTon,
                        fee = "≈ " + CurrencyFormatter.format("TON", amount),
                        feeInCurrency = "≈ " + CurrencyFormatter.formatFiat(currency.code, feeInCurrency),
                        buttonEnabled = true,
                        emulatedEventItems = actions
                    )
                }

            } catch (e: Throwable) {
                updateUiState {
                    it.copy(
                        feeValue = 0,
                        fee = "unknown",
                        buttonEnabled = true,
                    )
                }
            }
        }
    }

    private suspend fun getSeqno(wallet: WalletLegacy): Int {
        if (lastSeqno == 0 || lastSeqno == -1) {
            lastSeqno = wallet.getSeqno(api)
        }
        return lastSeqno
    }

}