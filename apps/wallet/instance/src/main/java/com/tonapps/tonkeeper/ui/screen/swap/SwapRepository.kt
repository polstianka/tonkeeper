package com.tonapps.tonkeeper.ui.screen.swap

import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.SwapDataEntity
import com.tonapps.wallet.data.account.legacy.WalletManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

class SwapRepository(
    private val walletManager: WalletManager,
    private val assetsRepository: AssetsRepository,
    private val api: API,
) {
    private val _swapData = MutableStateFlow<SwapDataEntity?>(null)
    val swapData: StateFlow<SwapDataEntity?> = _swapData

    private var _sendToken = MutableStateFlow<AssetModel?>(null)
    val sendToken: StateFlow<AssetModel?> = _sendToken

    private var _receiveToken = MutableStateFlow<AssetModel?>(null)
    val receiveToken: StateFlow<AssetModel?> = _receiveToken

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var simulateJob: Job? = null
    private var sendInput: String = "0"
    private var receiveInput: String = "0"
    private var tolerance: Float = 0.05f

    suspend fun init() {
        assetsRepository.get().firstOrNull()?.let {
            setSendToken(
                AssetModel(
                    token = it.token,
                    balance = it.value,
                    walletAddress = it.token.address,
                    position = ListCell.Position.SINGLE,
                    fiatBalance = 0f
                )
            )
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
        _sendToken.value = model
        reset()
    }

    fun setReceiveToken(model: AssetModel) {
        _receiveToken.value = model
        reset()
    }

    fun swap() {
        val tempReceive = _receiveToken.value
        _receiveToken.value = _sendToken.value
        _sendToken.value = tempReceive
        debounce { simulateSwap(sendInput) }
    }

    fun setSlippageTolerance(tolerance: Float) {
        this.tolerance = tolerance
    }

    private fun reset() {
        simulateJob?.cancel()
        sendInput = "0"
        receiveInput = "0"
        _swapData.update {
            it?.copy(askUnits = "0", offerUnits = "0")
        }
    }

    private suspend fun simulateSwap(units: String) = simulateSwap(units, false)

    private suspend fun simulateReverseSwap(units: String) = simulateSwap(units, true)

    private suspend fun simulateSwap(units: String, reverse: Boolean) {
        val send = _sendToken.value
        val ask = _receiveToken.value
        if (send != null && ask != null && units.isNotEmpty()) {
            val unitsBd = BigDecimal(units)
            if (unitsBd <= BigDecimal.ZERO) {
                _swapData.value = null
                return
            }

            val unitsConverted = unitsBd.movePointRight(send.token.decimals).toPlainString()
            walletManager.getWalletInfo()?.let {

                while (true) {
                    val data = if (reverse) api.simulateReverseSwap(
                        offerAddress = send.token.address,
                        askAddress = ask.token.address,
                        units = unitsConverted,
                        testnet = it.testnet,
                        tolerance = tolerance.toString()
                    ) else api.simulateSwap(
                        offerAddress = send.token.address,
                        askAddress = ask.token.address,
                        units = unitsConverted,
                        testnet = it.testnet,
                        tolerance = tolerance.toString()
                    )
                    _swapData.value = data.copy(
                        offerUnits = data.offerUnits.movePointLeft(send.token.decimals),
                        askUnits = data.askUnits.movePointLeft(ask.token.decimals)
                    )
                    delay(5000)
                }
            }
        }
    }

    private fun String.movePointLeft(decimals: Int): String {
        return BigDecimal(this).movePointLeft(decimals).stripTrailingZeros().toPlainString()
    }

    private fun debounce(millis: Long = 300L, block: suspend () -> Unit) {
        simulateJob?.cancel()
        simulateJob = scope.launch {
            delay(millis)
            block()
        }
    }
}
