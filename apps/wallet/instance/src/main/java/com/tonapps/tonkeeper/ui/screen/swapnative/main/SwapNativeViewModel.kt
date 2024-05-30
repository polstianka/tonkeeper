package com.tonapps.tonkeeper.ui.screen.swapnative.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.Coin
import com.tonapps.network.NetworkMonitor
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.ui.screen.swapnative.confirm.SwapConfirmArgs
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.AssetRepository
import com.tonapps.wallet.data.token.SwapRepository
import com.tonapps.wallet.data.token.TokenRepository
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import com.tonapps.wallet.data.token.entities.AssetEntity
import com.tonapps.wallet.data.token.entities.SwapSimulateEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SwapNativeViewModel(
    private val assetRepository: AssetRepository,
    private val swapRepository: SwapRepository,
    private val networkMonitor: NetworkMonitor,
    private val walletRepository: WalletRepository,
    private val settings: SettingsRepository,
    private val tokenRepository: TokenRepository,
    private val api: API,
) : ViewModel() {

    private val _screenStateFlow = MutableStateFlow(SwapNativeScreenState())
    val screenStateFlow: StateFlow<SwapNativeScreenState> = _screenStateFlow

    private var _selectedFromToken = MutableStateFlow<AssetEntity?>(null)
    var selectedFromToken: StateFlow<AssetEntity?> = _selectedFromToken

    private var _selectedToToken = MutableStateFlow<AssetEntity?>(null)
    var selectedToToken: StateFlow<AssetEntity?> = _selectedToToken

    private var _swapDetailsFlow = MutableStateFlow<SwapSimulateEntity?>(null)
    var swapDetailsFlow: StateFlow<SwapSimulateEntity?> = _swapDetailsFlow

    private var _selectedFromTokenAmount = MutableStateFlow<String>(DEFAULT_INPUT_AMOUNT_VALUE)
    var selectedFromTokenAmount: StateFlow<String> = _selectedFromTokenAmount
    private var _selectedToTokenAmount = MutableStateFlow<String>(DEFAULT_INPUT_AMOUNT_VALUE)
    var selectedToTokenAmount: StateFlow<String> = _selectedToTokenAmount

    private val _tokenListFlow = MutableStateFlow<List<AccountTokenEntity>>(emptyList())
    val selectedSlippageFlow = MutableStateFlow<Float>(DEFAULT_SLIPPAGE)

    // confirm swap
    var walletEntity: WalletEntity? = null
    var walletAddress: String? = null

    var isProgrammaticSet = false

    private var onToAmountChangedDebounceJob: Job? = null
    private var swapDetailGetRemoteJob: Job? = null

    init {
        combine(
            walletRepository.activeWalletFlow,
            settings.currencyFlow,
            networkMonitor.isOnlineFlow
        ) { wallet, currency, isOnline ->

            // confirm swap
            walletEntity = wallet
            walletAddress = App.walletManager.getWalletInfo()?.accountId
                ?: throw Exception("failed to get wallet")

            _tokenListFlow.value =
                tokenRepository.getLocal(currency, wallet.accountId, wallet.testnet)
            val assetMap = assetRepository.get(false).values.toList().associateBy { it.symbol }

            val hiddenBalance = settings.hiddenBalances
            _tokenListFlow.value.forEach { token ->
                assetMap.get(token.symbol)?.also {
                    it.balance = token.balance.value
                    it.rate = token.rateNow
                    it.hiddenBalance = hiddenBalance
                }
            }

            // put TON as initial selected asset
            if (selectedFromToken.value == null) {
                val ton = assetMap[AssetEntity.tonSymbol]
                setSelectedFromToken(ton)
            }

        }.launchIn(viewModelScope)

        combine(
            flow = selectedFromToken,
            flow2 = selectedToToken,
            flow3 = _selectedFromTokenAmount,
            flow4 = swapDetailsFlow
        ) { from, to, fromAmount, swap ->
            updateContinueButtonState()
        }.launchIn(viewModelScope)
    }

    var periodicSimulateJob: Job? = null
    private fun startPeriodicSwapSimulate(isReverse: Boolean) {
        periodicSimulateJob?.cancel()
        periodicSimulateJob = viewModelScope.launch {
            while (true) {
                delay(REPEAT_SIMULATE_INTERVAL)
                triggerSimulateSwap(isReverse)
            }
        }
    }

    fun stopPeriodicSwapSimulate() {
        periodicSimulateJob?.cancel()
    }

    fun setSelectedFromToken(sellAssetEntity: AssetEntity?, isSwitching: Boolean = false) {
        sellAssetEntity?.hiddenBalance = settings.hiddenBalances

        _swapDetailsFlow.value = null
        _selectedFromToken.value = sellAssetEntity
        _selectedFromTokenAmount.value = DEFAULT_INPUT_AMOUNT_VALUE

    }

    fun setSelectedToToken(buyAssetEntity: AssetEntity?, isSwitch: Boolean = false) {
        buyAssetEntity?.hiddenBalance = settings.hiddenBalances

        _swapDetailsFlow.value = null
        _selectedToToken.value = buyAssetEntity
        // TODO !!! do this if not same token
        _selectedToTokenAmount.value = DEFAULT_INPUT_AMOUNT_VALUE

        if (!isSwitch)
            triggerSimulateSwap(false)
    }

    private var onFromAmountChangedDebounceJob: Job? = null
    fun onFromAmountChanged(amount: String) {
        _selectedFromTokenAmount.value = amount

        onFromAmountChangedDebounceJob?.cancel()
        if (!isProgrammaticSet) {
            onFromAmountChangedDebounceJob = viewModelScope.launch {
                delay(AMOUNT_INPUT_DEBOUNCE)

                triggerSimulateSwap(false)
            }
        }
    }

    fun onToAmountChanged(amount: String) {
        _selectedToTokenAmount.value = amount

        onToAmountChangedDebounceJob?.cancel()
        if (!isProgrammaticSet) {
            onToAmountChangedDebounceJob = viewModelScope.launch {
                delay(AMOUNT_INPUT_DEBOUNCE)

                triggerSimulateSwap(true)
            }
        }
    }

    suspend fun getAssetByAddress(contractAdrress: String): AssetEntity? {
        val token = assetRepository.get(false)[contractAdrress]
        return token
    }

    fun triggerSimulateSwap(isReverse: Boolean) {
        if (
            _selectedFromToken.value != null &&
            _selectedToToken.value != null &&
            _selectedFromToken.value?.contractAddress != _selectedToToken.value?.contractAddress
        ) {

            stopPeriodicSwapSimulate()
            cancelPreviousSwapDetailRequests()

            val units = if (!isReverse) {
                Coin.toNanoDouble(
                    selectedFromTokenAmount.value.toDouble(),
                    _selectedFromToken.value!!.decimals
                ).toString()
            } else {
                Coin.toNanoDouble(
                    selectedToTokenAmount.value.toDouble(),
                    _selectedToToken.value!!.decimals
                ).toString()
            }

            simulateSwap(
                _selectedFromToken.value!!.contractAddress,
                _selectedToToken.value!!.contractAddress,
                units,
                (selectedSlippageFlow.value / 100).toString(),
                isReverse
            )
        }
    }

    private fun simulateSwap(
        sellAddress: String,
        buyAddress: String,
        units: String,
        slippage: String,
        isReverse: Boolean
    ) {
        _screenStateFlow.update {
            it.copy(continueState = SwapNativeScreenState.ContinueState.LOADING)
        }

        swapDetailGetRemoteJob = viewModelScope.launch {
            val swapDetails = swapRepository.simulate(
                sellAddress,
                buyAddress,
                units,
                slippage,
                isReverse,
                false
            )

            if (swapDetails != null) {
                // auto fetch
                startPeriodicSwapSimulate(isReverse)

                // add decimals to this object
                swapDetails.fromDecimals = selectedFromToken.value?.decimals ?: 0
                swapDetails.toDecimals = selectedToToken.value?.decimals ?: 0
            }

            // todo add this
            if (swapDetails == null && selectedFromTokenAmount.value == "0") {
                // set to amount 0
            }

            updateContinueButtonState()
            _swapDetailsFlow.value = swapDetails
        }
    }

    fun cancelPreviousSwapDetailRequests() {
        swapDetailGetRemoteJob?.cancel()
    }

    private fun updateContinueButtonState() {
        val from = selectedFromToken.value
        val fromAmount = selectedFromTokenAmount.value
        val to = selectedToToken.value
        val swap = swapDetailsFlow.value

        val state = when {
            from == null -> SwapNativeScreenState.ContinueState.SELECT_TOKEN
            fromAmount == DEFAULT_INPUT_AMOUNT_VALUE -> SwapNativeScreenState.ContinueState.ENTER_AMOUNT
            fromAmount.toBigDecimal() > from.balance.toBigDecimal() -> {
                if (from.isTon)
                    SwapNativeScreenState.ContinueState.INSUFFICIENT_TON_BALANCE
                else SwapNativeScreenState.ContinueState.INSUFFICIENT_BALANCE
            }

            to == null -> SwapNativeScreenState.ContinueState.SELECT_TOKEN
            swap == null -> SwapNativeScreenState.ContinueState.DISABLE
            else -> SwapNativeScreenState.ContinueState.NEXT
        }

        _screenStateFlow.update {
            it.copy(continueState = state)
        }
    }

    fun generateConfirmArgs(): SwapConfirmArgs? {
        return if (
            selectedFromToken.value != null &&
            selectedToToken.value != null &&
            swapDetailsFlow.value != null
        ) {
            SwapConfirmArgs(
                selectedFromToken.value,
                selectedToToken.value,
                swapDetailsFlow.value
            )
        } else null
    }

    companion object {
        const val DEFAULT_SLIPPAGE = 0.1f

        const val REPEAT_SIMULATE_INTERVAL = 5000L
        const val AMOUNT_INPUT_DEBOUNCE = 500L

        const val DEFAULT_INPUT_AMOUNT_VALUE = "0"
    }

}