package com.tonapps.tonkeeper.ui.screen.action

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.ton.extensions.base64
import com.tonapps.ledger.ton.Transaction
import com.tonapps.tonkeeper.extensions.signLedgerTransaction
import com.tonapps.tonkeeper.ui.base.BaseWalletVM
import com.tonapps.tonkeeper.ui.screen.send.main.SendException
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.account.AccountRepository
import com.tonapps.wallet.data.battery.BatteryRepository
import com.tonapps.wallet.data.passcode.PasscodeManager
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import org.ton.contract.wallet.WalletTransfer

class ActionViewModel(
    app: Application,
    private val args: ActionArgs,
    private val accountRepository: AccountRepository,
    private val passcodeManager: PasscodeManager,
    private val api: API,
    private val batteryRepository: BatteryRepository,
) : BaseWalletVM(app) {

    private val _walletFlow = MutableStateFlow<WalletEntity?>(null)
    val walletFlow = _walletFlow.asStateFlow().filterNotNull()

    init {
        viewModelScope.launch {
            _walletFlow.value = accountRepository.getWalletById(args.walletId)
        }
    }

    fun sign(context: Context) = walletFlow.take(1).flatMapLatest { w ->
        val passcodeFlow = if (!w.isLedger) {
            passcodeManager.confirmationFlow(context, context.getString(Localization.app_name))
                .take(1)
        } else {
            flowOf(Unit)
        }

        combine(
            passcodeFlow,
            flowOf(w),
        ) { _, wallet ->
            val request = args.request
            val seqno = accountRepository.getSeqno(wallet)
            if (wallet.isLedger) {
                val transactions = request.transfers.map { transfer ->
                    Transaction.fromWalletTransfer(
                        transfer,
                        seqno = seqno,
                        timeout = request.validUntil
                    )
                }

                var boc: String? = null

                transactions.forEachIndexed { index, transaction ->
                    Log.d("ActionViewModel", "Signing transaction $transaction")
                    val isLast = index == transactions.size - 1
                    val signedBody = context.signLedgerTransaction(transaction, wallet.id)
                        ?: throw SendException.Cancelled()
                    val contract = wallet.contract
                    val message =
                        contract.createTransferMessageCell(contract.address, seqno, signedBody)
                    boc = message.base64()
                    if (!isLast) {
                        Log.d("ActionViewModel", "Sending transaction to blockchain")
                        if (!api.sendToBlockchain(message, wallet.testnet)) {
                            throw SendException.FailedToSendTransaction()
                        }
                    }
                }

                if (boc == null) throw SendException.UnableSendTransaction()

                boc!!
            } else {
                val secretKey = accountRepository.getPrivateKey(wallet.id)
                val excessesAddress =
                    if (args.isBattery) batteryRepository.getConfig(wallet.testnet)
                        .getExcessesAddress() else null

                val transfers = request.messages.map { it.getWalletTransfer(excessesAddress) }

                val message = accountRepository.createSignedMessage(
                    wallet,
                    seqno,
                    secretKey,
                    request.validUntil,
                    transfers,
                    internalMessage = args.isBattery
                )
                message.base64()
            }
        }
    }

}