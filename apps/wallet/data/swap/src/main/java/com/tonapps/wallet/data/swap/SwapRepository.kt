package com.tonapps.wallet.data.swap

import com.tonapps.blockchain.Coin
import com.tonapps.blockchain.ton.TonNetwork
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.SwapDetailsEntity
import com.tonapps.wallet.data.account.SeqnoHelper
import com.tonapps.wallet.data.account.legacy.WalletManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.ton.block.Coins
import org.ton.block.MsgAddressInt
import ton.Swap
import ton.TransactionHelper
import ton.transfer.Transfer
import java.math.BigDecimal
import kotlin.time.Duration.Companion.seconds

//router address == owner address
//then use ask address and EQCM3B12QK1e4yZSf8GtBRT0aLMNyEsBc_DhVfRRtOEffLez

private const val PROXY_TON = "EQCM3B12QK1e4yZSf8GtBRT0aLMNyEsBc_DhVfRRtOEffLez"

private const val Commission = 215000000L

class SwapRepository(
    private val walletManager: WalletManager,
    private val api: API
) {
    private val _swapState = MutableStateFlow(SwapState())
    val swapState: StateFlow<SwapState> = _swapState

    private val _signRequestEntity = MutableStateFlow<SwapSignRequestEntity?>(null)
    val signRequestEntity: StateFlow<SwapSignRequestEntity?> = _signRequestEntity

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var simulateJob: Job? = null
    private var sendInput: String = "0"
    private var receiveInput: String = "0"
    private var tolerance: Float = 0.05f
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
        runSimulateSwapConditionally(sendInput)
    }

    fun setReceiveToken(model: AssetModel) {
        receiveInput = "0"
        _swapState.update {
            it.copy(receive = model)
        }
        runSimulateSwapConditionally(sendInput)
    }

    fun swap() {
        _swapState.update {
            it.copy(details = null, send = it.receive, receive = it.send, reversed = !it.reversed)
        }
        runSimulateSwapConditionally(if (_swapState.value.reversed) sendInput else receiveInput)
    }

    fun onConfirmSwapClick() {
        scope.launch {
            val routerAddress = _swapState.value.details?.routerAddress
            if (routerAddress != null) {
                val proxyTonAddress = async {
                    api.getJettonAddress(
                        ownerAddress = routerAddress,
                        jettonAddress = PROXY_TON,
                        testnet = testnet
                    )
                }.await()
                val askJettonAddress = async {
                    api.getJettonAddress(
                        ownerAddress = routerAddress,
                        jettonAddress = _swapState.value.receive?.token?.address.orEmpty(),
                        testnet = testnet
                    )
                }.await()

                val userWallet = walletManager.getWalletInfo()!!
                val swapPayload = Swap.swap(
                    askAddress = MsgAddressInt.parse(askJettonAddress),
                    userAddressInt = MsgAddressInt.parse(userWallet.address),
                    coins = Coins.Companion.ofNano(
                        BigDecimal(_swapState.value.details?.minReceived).toLong()
                    )
                )
                val outputValue = BigDecimal(sendInput).movePointRight(9).toLong()
                val transferPayload = Transfer.jetton(
                    coins = Coins.Companion.ofNano(outputValue + Commission),
                    toAddress = MsgAddressInt.parse(routerAddress),
                    responseAddress = null,
                    forwardAmount = Commission, //commission?
                    queryId = TransactionHelper.getWalletQueryId(),
                    body = swapPayload
                )

                val gift = TransactionHelper.buildWalletTransfer(
                    destination = MsgAddressInt.parse(proxyTonAddress),
                    stateInit = SeqnoHelper.getStateInitIfNeed(userWallet, api),
                    body = transferPayload,
                    coins = Coins.Companion.ofNano(outputValue + Commission)
                )

                val signRequestEntity = SwapSignRequestEntity(
                    fromValue = "",
                    validUntil = (Clock.System.now() + 60.seconds).epochSeconds,
                    walletTransfer = gift,
                    network = if (userWallet.testnet) TonNetwork.TESTNET else TonNetwork.MAINNET
                )

                _signRequestEntity.value = signRequestEntity
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
        _signRequestEntity.value = null
    }

    private fun runSimulateSwapConditionally(units: String) {
        debounce {
            simulateSwap(units, _swapState.value.reversed)
        }
    }

    private suspend fun CoroutineScope.simulateSwap(units: String) =
        simulateSwap(units, false)

    private suspend fun CoroutineScope.simulateReverseSwap(units: String) =
        simulateSwap(units, true)

    private suspend fun CoroutineScope.simulateSwap(units: String, reverse: Boolean) {
        val send = _swapState.value.send
        val ask = _swapState.value.receive
        val unitsPrepared = Coin.prepareValue(units)

        if (send != null && ask != null && unitsPrepared.isNotEmpty()) {
            val unitsBd = BigDecimal(unitsPrepared)
            if (unitsBd <= BigDecimal.ZERO) {
                _swapState.update { it.copy(details = null) }
                return
            }
            val decimals = if (reverse) ask.token.decimals else send.token.decimals
            val unitsConverted = Coin.toNano(unitsBd.toFloat(), decimals).toString()
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
                    ensureActive()
                    _swapState.update {
                        val offerUnits =
                            Coin.toCoins(data.offerUnits.toLong(), send.token.decimals).toString()
                        val askUnits =
                            Coin.toCoins(data.askUnits.toLong(), ask.token.decimals).toString()

                        sendInput = offerUnits
                        receiveInput = askUnits

                        it.copy(
                            details = data.copy(
                                offerUnits = offerUnits,
                                askUnits = askUnits,
                                minReceived = Coin.toCoins(
                                    data.minReceived.toLong(),
                                    ask.token.decimals
                                ).toString(),
                                providerFeeUnits = Coin.toCoins(data.providerFeeUnits.toLong())
                                    .toString()
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
    val details: SwapDetailsEntity? = null,
    val reversed: Boolean = false
)
