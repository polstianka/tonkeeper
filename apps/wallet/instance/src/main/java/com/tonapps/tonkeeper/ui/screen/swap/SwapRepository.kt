package com.tonapps.tonkeeper.ui.screen.swap

import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.SwapDetailsEntity
import com.tonapps.wallet.data.account.legacy.WalletManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.math.BigDecimal

//router address == owner address
//then use ask address and EQCM3B12QK1e4yZSf8GtBRT0aLMNyEsBc_DhVfRRtOEffLez

private const val PROXY_TON = "EQCM3B12QK1e4yZSf8GtBRT0aLMNyEsBc_DhVfRRtOEffLez"

class SwapRepository(
    private val walletManager: WalletManager,
    private val api: API,
) {
    private val _swapState = MutableStateFlow(SwapState())
    val swapState: StateFlow<SwapState> = _swapState

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var simulateJob: Job? = null
    private var sendInput: String = "0"
    private var receiveInput: String = "0"
    private var tolerance: Float = 0.05f
    private var isSwapped: Boolean = false
    private var testnet: Boolean = false

    init {
        scope.launch {
            testnet = walletManager.getWalletInfo()?.testnet ?: false
        }
    }

    fun sendTextChanged(s: String) {
        sendInput = s
        debounce { simulateSwap(s) }
    }

    fun receiveTextChanged(s: String) {
        receiveInput = s
        debounce { simulateReverseSwap(s) }
    }

    fun setSendToken(model: AssetModel) {
        _swapState.update {
            it.copy(send = model)
        }
        runSimulateSwapConditionally()
    }

    fun setReceiveToken(model: AssetModel) {
        receiveInput = "0"
        _swapState.update {
            it.copy(receive = model)
        }
        runSimulateSwapConditionally()
    }

    fun swap() {
        _swapState.update {
            it.copy(
                send = it.receive,
                receive = it.send,
                details = null
            )
        }

        val tempReceiveInput = receiveInput
        receiveInput = sendInput
        sendInput = tempReceiveInput

        isSwapped = !isSwapped
        runSimulateSwapConditionally()
    }

    fun onContinueSwapClick() {
        scope.launch {
            val routerAddress = _swapState.value.details?.routerAddress
            if (routerAddress != null) {

                val address1Req = async {
                    api.getJettonAddress(
                        ownerAddress = routerAddress,
                        address = PROXY_TON,
                        testnet = testnet
                    )
                }
                val address2Req = async {
                    api.getJettonAddress(
                        ownerAddress = routerAddress,
                        address = _swapState.value.receive?.token?.address.orEmpty(),
                        testnet = testnet
                    )
                }

                val (addr1, addr2) = Pair(address1Req.await(), address2Req.await())
            }
        }
    }

    fun setSlippageTolerance(tolerance: Float) {
        this.tolerance = tolerance
    }

    fun clear() {
        simulateJob?.cancel()
        sendInput = "0"
        receiveInput = "0"
        _swapState.value = SwapState()
        tolerance = 0.05f
        isSwapped = false
    }

    private fun runSimulateSwapConditionally() {
        val units = if (isSwapped) receiveInput else sendInput
        debounce {
            simulateSwap(units, isSwapped)
        }
    }

    private suspend fun simulateSwap(units: String) =
        coroutineScope { simulateSwap(units, false) }

    private suspend fun simulateReverseSwap(units: String) =
        coroutineScope { simulateSwap(units, true) }

    private suspend fun CoroutineScope.simulateSwap(units: String, reverse: Boolean) {
        val send = _swapState.value.send
        val ask = _swapState.value.receive

        if (send != null && ask != null && units.isNotEmpty()) {
            val unitsBd = BigDecimal(units)
            if (unitsBd <= BigDecimal.ZERO) {
                _swapState.update { it.copy(details = null) }
                return
            }

            val unitsConverted = unitsBd.movePointRight(send.token.decimals).toPlainString()
            while (isActive) {
                try {
                    _swapState.update { it.copy(isLoading = true) }
                    val data = api.simulateSwap(
                        offerAddress = send.token.address,
                        askAddress = ask.token.address,
                        units = unitsConverted,
                        testnet = testnet,
                        tolerance = tolerance.toString(),
                        reverse = reverse
                    )
                    _swapState.update {
                        it.copy(
                            details = data.copy(
                                offerUnits = data.offerUnits.movePointLeft(send.token.decimals),
                                askUnits = data.askUnits.movePointLeft(ask.token.decimals),
                                minReceived = data.minReceived.movePointLeft(ask.token.decimals),
                                providerFeeUnits = data.providerFeeUnits.movePointLeft(ask.token.decimals)
                            ),
                            isLoading = false
                        )
                    }
                    delay(5000)
                } catch (e: Exception) {
                    println(e.message)
                }

            }
        }
    }

    private fun String.movePointLeft(decimals: Int): String {
        return BigDecimal(this).movePointLeft(decimals).stripTrailingZeros().toPlainString()
    }

    private fun debounce(millis: Long = 300L, block: suspend CoroutineScope.() -> Unit) {
        simulateJob?.cancel()
        simulateJob = scope.launch {
            delay(millis)
            block(this)
        }
    }
}

data class SwapState(
    val send: AssetModel? = null,
    val receive: AssetModel? = null,
    val isLoading: Boolean = false,
    val details: SwapDetailsEntity? = null
)
