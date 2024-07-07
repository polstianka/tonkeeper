package com.tonapps.wallet.data.swap

import com.tonapps.blockchain.Coin
import com.tonapps.blockchain.ton.TonNetwork
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.SwapDetailsEntity
import com.tonapps.wallet.data.account.SeqnoHelper
import com.tonapps.wallet.data.account.legacy.WalletManager
import com.tonapps.wallet.data.swap.SwapState.Companion.getSwapType
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
import ton.Swap.JETTON_TO_JETTON_FORWARD_GAS
import ton.Swap.JETTON_TO_JETTON_GAS
import ton.Swap.JETTON_TO_TON_FORWARD_GAS
import ton.Swap.JETTON_TO_TON_GAS
import ton.Swap.PROXY_TON
import ton.Swap.TON_TO_JETTON_FORWARD_GAS
import ton.TransactionHelper
import ton.transfer.Transfer
import java.math.BigDecimal
import kotlin.time.Duration.Companion.seconds


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
    private var tonAsset: AssetModel? = null

    init {
        scope.launch {
            testnet = walletManager.getWalletInfo()?.testnet ?: false
        }
    }

    fun setTonInfo(tonAsset: AssetModel) {
        this.tonAsset = tonAsset
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
            when (_swapState.value.getSwapType()) {
                SwapType.TonToJetton -> runTonToJetton()
                SwapType.JettonToTon -> runJettonToTon()
                SwapType.JettonToJetton -> runJettonToJetton()
            }
        }
    }

    private suspend fun CoroutineScope.runJettonToJetton() {
        val userWallet = walletManager.getWalletInfo() ?: error("No wallet info")
        val fromWalletAddress = _swapState.value.send?.token?.address
        val routerAddress = _swapState.value.details?.routerAddress
        val userWalletAddress = tonAsset?.walletAddress
        val toWalletAddress = _swapState.value.receive?.token?.address

        if (fromWalletAddress != null && routerAddress != null && userWalletAddress != null && toWalletAddress != null) {
            val fromAddress = async {
                api.getJettonAddress(
                    ownerAddress = userWalletAddress,
                    jettonAddress = fromWalletAddress,
                    testnet = testnet
                )
            }.await()
            val toAddress = async {
                api.getJettonAddress(
                    ownerAddress = routerAddress,
                    jettonAddress = toWalletAddress,
                    testnet = testnet
                )
            }.await()

            val swapPayload = Swap.swapTonToJetton(
                toAddress = MsgAddressInt.parse(toAddress),
                userAddressInt = MsgAddressInt.parse(userWallet.address),
                coins = Coins.ofNano(
                    BigDecimal(_swapState.value.details?.minReceived)
                        .movePointRight(_swapState.value.receive?.token?.decimals ?: TON_DECIMALS)
                        .toLong()
                )
            )
            val outputValue =
                BigDecimal(sendInput).movePointRight(
                    _swapState.value.send?.token?.decimals ?: TON_DECIMALS
                )
                    .toLong()
            val transferPayload = Transfer.jetton(
                coins = Coins.ofNano(outputValue),
                toAddress = MsgAddressInt.parse(routerAddress),
                responseAddress = MsgAddressInt.parse(userWallet.address),
                forwardAmount = JETTON_TO_JETTON_GAS,
                queryId = TransactionHelper.getWalletQueryId(),
                body = swapPayload
            )

            val gift = TransactionHelper.buildWalletTransfer(
                destination = MsgAddressInt.parse(fromAddress),
                stateInit = SeqnoHelper.getStateInitIfNeed(userWallet, api),
                body = transferPayload,
                coins = Coins.ofNano(JETTON_TO_JETTON_FORWARD_GAS)
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

    private suspend fun CoroutineScope.runJettonToTon() {
        val fromWalletAddress = _swapState.value.send?.token?.address
        val routerAddress = _swapState.value.details?.routerAddress
        val userWalletAddress = tonAsset?.walletAddress
        val userWallet = walletManager.getWalletInfo() ?: error("No wallet info")

        if (fromWalletAddress != null && routerAddress != null && userWalletAddress != null) {
            val fromAddress = async {
                api.getJettonAddress(
                    ownerAddress = userWalletAddress,
                    jettonAddress = fromWalletAddress,
                    testnet = testnet
                )
            }.await()
            val toAddress = async {
                api.getJettonAddress(
                    ownerAddress = routerAddress,
                    jettonAddress = PROXY_TON,
                    testnet = testnet
                )
            }.await()

            val swapPayload = Swap.swapTonToJetton(
                toAddress = MsgAddressInt.parse(toAddress),
                userAddressInt = MsgAddressInt.parse(userWallet.address),
                coins = Coins.ofNano(
                    BigDecimal(_swapState.value.details?.minReceived)
                        .movePointRight(_swapState.value.receive?.token?.decimals ?: TON_DECIMALS)
                        .toLong()
                )
            )
            val outputValue = BigDecimal(sendInput)
                .movePointRight(_swapState.value.send?.token?.decimals ?: TON_DECIMALS)
                .toLong()
            val transferPayload = Transfer.jetton(
                coins = Coins.ofNano(outputValue),
                toAddress = MsgAddressInt.parse(routerAddress),
                responseAddress = MsgAddressInt.parse(userWallet.address),
                forwardAmount = JETTON_TO_TON_FORWARD_GAS,
                queryId = TransactionHelper.getWalletQueryId(),
                body = swapPayload
            )

            val gift = TransactionHelper.buildWalletTransfer(
                destination = MsgAddressInt.parse(fromAddress),
                stateInit = SeqnoHelper.getStateInitIfNeed(userWallet, api),
                body = transferPayload,
                coins = Coins.ofNano(JETTON_TO_TON_GAS)
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

    private suspend fun CoroutineScope.runTonToJetton() {
        val routerAddress = _swapState.value.details?.routerAddress
        if (routerAddress != null) {
            val fromTonAddress = async {
                api.getJettonAddress(
                    ownerAddress = routerAddress,
                    jettonAddress = PROXY_TON,
                    testnet = testnet
                )
            }.await()
            val toJettonAddress = async {
                api.getJettonAddress(
                    ownerAddress = routerAddress,
                    jettonAddress = _swapState.value.receive?.token?.address.orEmpty(),
                    testnet = testnet
                )
            }.await()

            val userWallet = walletManager.getWalletInfo() ?: error("No wallet info")
            val swapPayload = Swap.swapTonToJetton(
                toAddress = MsgAddressInt.parse(toJettonAddress),
                userAddressInt = MsgAddressInt.parse(userWallet.address),
                coins = Coins.ofNano(
                    BigDecimal(_swapState.value.details?.minReceived)
                        .movePointRight(_swapState.value.receive?.token?.decimals ?: TON_DECIMALS)
                        .toLong()
                )
            )
            val outputValue = BigDecimal(sendInput).movePointRight(TON_DECIMALS).toLong()
            val transferPayload = Transfer.jetton(
                coins = Coins.ofNano(outputValue),
                toAddress = MsgAddressInt.parse(routerAddress),
                responseAddress = null,
                forwardAmount = TON_TO_JETTON_FORWARD_GAS,
                queryId = TransactionHelper.getWalletQueryId(),
                body = swapPayload
            )

            val gift = TransactionHelper.buildWalletTransfer(
                destination = MsgAddressInt.parse(fromTonAddress),
                stateInit = SeqnoHelper.getStateInitIfNeed(userWallet, api),
                body = transferPayload,
                coins = Coins.ofNano(outputValue + TON_TO_JETTON_FORWARD_GAS)
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

    companion object {
        private const val TON_DECIMALS = 9
    }
}

data class SwapState(
    val send: AssetModel? = null,
    val receive: AssetModel? = null,
    val isLoading: Boolean = false,
    val details: SwapDetailsEntity? = null,
    val reversed: Boolean = false
) {
    companion object {
        fun SwapState.getSwapType(): SwapType {
            return when {
                send?.isTon == true -> SwapType.TonToJetton
                receive?.isTon == true -> SwapType.JettonToTon
                else -> SwapType.JettonToJetton
            }
        }
    }
}

enum class SwapType {
    TonToJetton, JettonToTon, JettonToJetton
}
