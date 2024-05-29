package com.tonapps.tonkeeper.helper.flow

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.tonapps.tonkeeper.core.signer.SingerResultContract
import com.tonapps.tonkeeper.event.WalletStateUpdateEvent
import com.tonapps.tonkeeper.password.PasscodeRepository
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.WalletType
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.account.legacy.WalletManager
import core.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.ton.api.pub.PublicKeyEd25519
import org.ton.bitstring.BitString
import org.ton.cell.Cell
import org.ton.crypto.base64
import uikit.widget.ProcessTaskView

class TransactionSender(
    private val api: API,
    private val passcodeRepository: PasscodeRepository,
    private val walletManager: WalletManager,
    private val coroutineScope: CoroutineScope
) {
    private val _sendingStateFlow = MutableStateFlow(
        SendingState(
            processActive = false,
            processState = ProcessTaskView.State.LOADING
        )
    )
    val statusFlow = _sendingStateFlow.asStateFlow()

    private val _uiEffect = MutableSharedFlow<Effect>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val effectsFlow: SharedFlow<Effect?> = _uiEffect.asSharedFlow()

    private var lastSignerRequest: SignerRequest? = null

    private data class SignerRequest(
        val walletEntity: WalletEntity,
        val unsignedBody: Cell,
        val seqno: Int
    )

    suspend fun send(context: Context, request: TransactionEmulator.Request) {
        if (_sendingStateFlow.value.processActive) {
            return
        }

        try {
            val wallet = request.getWallet()
            val transfer = request.getTransfer()

            if (wallet.type == WalletType.Signer) {
                val (unsignedBody, seqno) = wallet.buildUnsignedBody(api, transfer)
                lastSignerRequest = SignerRequest(wallet, unsignedBody, seqno)
                _uiEffect.tryEmit(Effect.OpenSignerApp(body = unsignedBody, publicKey = wallet.publicKey))
            } else {
                _sendingStateFlow.value = _sendingStateFlow.value.copy(
                    processActive = true,
                    processState = ProcessTaskView.State.LOADING
                )

                if (!passcodeRepository.confirmation(context)) {
                    throw Exception("failed to request passcode")
                }

                wallet.send(api, walletManager.getPrivateKey(wallet.id), transfer)
                successResult()
            }
        } catch (e: Throwable) {
            failedResult()
        }
    }

    private fun sendSignature(data: ByteArray) {
        _sendingStateFlow.value = _sendingStateFlow.value.copy(
            processActive = true,
            processState = ProcessTaskView.State.LOADING
        )

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val wallet = lastSignerRequest?.walletEntity ?: throw Exception("failed to get wallet")
                val contract = wallet.contract

                val unsignedBody = lastSignerRequest?.unsignedBody ?: throw Exception("unsigned body is null")
                val seqno = lastSignerRequest?.seqno ?: 0

                val signature = BitString(data)
                val signerBody = contract.signedBody(signature, unsignedBody)

                val b = contract.createTransferMessageCell(wallet.contract.address, seqno, signerBody)
                wallet.send(api, b)

                successResult()
            } catch (e: Throwable) {
                failedResult()
            }
        }
    }

    private fun setFailedResult() {
        coroutineScope.launch {
            failedResult()
        }
    }

    private suspend fun failedResult() {
        _sendingStateFlow.value =
            _sendingStateFlow.value.copy(processState = ProcessTaskView.State.FAILED)

        delay(5000)
        _uiEffect.tryEmit(Effect.CloseScreen(false))
    }

    private suspend fun successResult() {
        _sendingStateFlow.value =
            _sendingStateFlow.value.copy(processState = ProcessTaskView.State.SUCCESS)
        EventBus.post(WalletStateUpdateEvent)

        delay(1000)
        _uiEffect.tryEmit(Effect.CloseScreen(true))
    }

    data class SendingState(
        val processActive: Boolean = false,
        val processState: ProcessTaskView.State = ProcessTaskView.State.LOADING,
    )

    sealed class Effect {
        data class CloseScreen(
            val navigateToHistory: Boolean
        ) : Effect()

        data class OpenSignerApp(
            val body: Cell,
            val publicKey: PublicKeyEd25519,
        ): Effect()
    }

    /* * */

    class FragmentSenderController(private val fragment: Fragment, private val sender: TransactionSender) {
        private val signerLauncher = fragment.registerForActivityResult(SingerResultContract()) {
            if (it == null) {
                sender.setFailedResult()
            } else {
                sender.sendSignature(base64(it))
            }
        }

        fun attach(onClose: (close: Effect.CloseScreen) -> Unit) {
            fragment.viewLifecycleOwner.lifecycleScope.launch {
                sender.effectsFlow.collect { effect ->
                    if (effect is Effect.CloseScreen) {
                        onClose(effect)
                    } else if (effect is Effect.OpenSignerApp) {
                        signerLauncher.launch(SingerResultContract.Input(effect.body, effect.publicKey))
                    }
                }
            }
        }
    }
}