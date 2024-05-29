package com.tonapps.tonkeeper.ui.screen.swap.main

import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.Coin
import com.tonapps.blockchain.ton.extensions.EmptyPrivateKeyEd25519
import com.tonapps.network.NetworkMonitor
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.api.totalFees
import com.tonapps.tonkeeper.core.signer.SingerResultContract
import com.tonapps.tonkeeper.core.toDefaultCoinAmount
import com.tonapps.tonkeeper.core.toDisplayAmount
import com.tonapps.tonkeeper.extensions.sendToBlockchain
import com.tonapps.tonkeeper.password.PasscodeRepository
import com.tonapps.tonkeeper.ui.screen.swap.data.AssetEntity
import com.tonapps.tonkeeper.ui.screen.swap.data.FormattedDecimal
import com.tonapps.tonkeeper.ui.screen.swap.data.PairMap
import com.tonapps.tonkeeper.ui.screen.swap.data.RemoteAssets
import com.tonapps.tonkeeper.ui.screen.swap.data.SimulationResult
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapAmount
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapConfig
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapConfig.Companion.LOGGING_ENABLED
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapConfig.Companion.LOGGING_TAG
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapConfig.Companion.debugTimestamp
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapEntity
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapOperationDetails
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapOperationError
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapRequest
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapSettings
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapSimulation
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapState
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapTarget
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapTimeout
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapTransfer
import com.tonapps.tonkeeper.ui.screen.swap.data.createPair
import com.tonapps.tonkeeper.ui.screen.swap.data.isSameToken
import com.tonapps.tonkeeper.ui.screen.swap.data.prepareSwapTransferDetails
import com.tonapps.tonkeeper.ui.screen.swap.data.toUserVisibleMessage
import com.tonapps.tonkeeper.ui.screen.swap.data.toWalletTransfer
import com.tonapps.tonkeeper.ui.screen.swap.stonfi.toAssetEntity
import com.tonapps.tonkeeper.ui.screen.swap.stonfi.toSimulationEntity
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.account.legacy.WalletManager
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.rates.entity.RatesEntity
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.TokenRepository
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.ton.bitstring.BitString
import org.ton.block.StateInit
import java.math.BigDecimal

data class SwapModelArgs(
    val localId: String,
    val walletAddress: String,
    val sendToken: TokenEntity?,
    val receiveToken: TokenEntity?,
    val confirmationRequest: SwapTransfer?
) {
    val isConfirmation: Boolean
        get() = confirmationRequest != null
}

@OptIn(FlowPreview::class)
class SwapViewModel(
    private val walletManager: WalletManager,
    private val tokenRepository: TokenRepository,
    private val walletRepository: WalletRepository,
    private val networkMonitor: NetworkMonitor,
    private val passcodeRepository: PasscodeRepository,
    private val ratesRepository: RatesRepository,
    private val settingsRepository: SettingsRepository,
    private val api: API,
    private val args: SwapModelArgs
) : ViewModel() {
    private val _swapState = MutableStateFlow(SwapState(
        walletAddress = args.walletAddress,
        send = args.confirmationRequest?.getSwapEntity(SwapTarget.SEND) ?: args.sendToken?.let { SwapEntity.valueOf(it) } ?: SwapEntity.EMPTY,
        receive = args.confirmationRequest?.getSwapEntity(SwapTarget.RECEIVE) ?: args.receiveToken?.let { SwapEntity.valueOf(it) } ?: SwapEntity.EMPTY,
        transfer = args.confirmationRequest
    ))
    val swapState: StateFlow<SwapState> = _swapState.asStateFlow()

    private val _remoteAssets = MutableStateFlow(RemoteAssets())
    val remoteAssets: StateFlow<RemoteAssets> = _remoteAssets.asStateFlow()

    private val _marketList = MutableStateFlow(PairMap())
    val marketList: StateFlow<PairMap> = _marketList.asStateFlow()

    private val _accountTokens = MutableStateFlow<List<AccountTokenEntity>>(emptyList())
    val accountTokens: StateFlow<List<AccountTokenEntity>> = _accountTokens.asStateFlow()

    data class LoadedData(
        val uiState: SwapState,
        val remoteAssets: RemoteAssets,
        val marketList: PairMap
    )

    val loadedData: Flow<LoadedData> = combine(swapState, remoteAssets, marketList) { state, assets, list ->
        LoadedData(state, assets, list)
    }.filter {
        !it.remoteAssets.isEmpty() && !it.marketList.isEmpty()
    }

    companion object {
        const val TRY_AGAIN_TIMEOUT = 2500L
        private var localIdCounter: Long = 0

        @Synchronized
        fun newLocalId(): String {
            val localId = ++localIdCounter
            return localId.toString()
        }

        private var viewModelCount = 0

        private fun <T> MutableStateFlow<T>.updateIf(condition: (T) -> Boolean, function: (T) -> T) {
            this.update {
                if (condition(it)) {
                    function(it)
                } else {
                    it
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (LOGGING_ENABLED) {
            Log.i(LOGGING_TAG, "[CONTEXT][${--viewModelCount}] - SWAP VIEW MODEL DESTROYED: localId = ${args.localId}")
        }
    }

    init {
        if (LOGGING_ENABLED) {
            Log.i(LOGGING_TAG, "[CONTEXT][${++viewModelCount}] + SWAP VIEW MODEL CREATED: localId = ${args.localId}")
        }
        if (!args.isConfirmation) {
            viewModelScope.launch(Dispatchers.IO) {
                loadData()
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            swapState
                .filter { it.canRunSimulations }
                .distinctUntilChanged { old, new ->
                    (old.requiresSimulationUpdate == new.requiresSimulationUpdate || !new.requiresSimulationUpdate) &&
                    old.settings == new.settings &&
                    old.receive.token.isSameToken(new.receive.token) &&
                    old.send.token.isSameToken(new.send.token)
                }.debounce(SwapConfig.APP_INPUT_DEBOUNCE_TIMEOUT_MS).collect {
                if (LOGGING_ENABLED) {
                    Log.v(LOGGING_TAG, "[${debugTimestamp()}] requesting simulation by data change...")
                }
                simulateSwap()
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            val periodicUpdatesFlow = flow {
                while (currentCoroutineContext().isActive) {
                    val state = swapState.first()
                    val isOnline = networkMonitor.isOnlineFlow.first()
                    emit(state.canRunSimulations && isOnline)
                    delay(SwapConfig.APP_SIMULATIONS_REFETCH_INTERVAL_MS)
                }
                if (LOGGING_ENABLED) {
                    Log.w(LOGGING_TAG, "[${debugTimestamp()}] No more simulation updates...")
                }
            }
            periodicUpdatesFlow.filter { it }.collect {
                if (LOGGING_ENABLED) {
                    Log.v(LOGGING_TAG, "[${debugTimestamp()}] requesting simulation by periodic updates...")
                }
                simulateSwap()
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            networkMonitor.isOnlineFlow.distinctUntilChanged().collect { isOnline ->
                if (isOnline) {
                    simulateSwap()
                }
            }
        }
        if (LOGGING_ENABLED) {
            viewModelScope.launch(Dispatchers.IO) {
                swapState.collect {
                    Log.d(LOGGING_TAG, "[${debugTimestamp()}] updateSwapState SEND:${it.send.amount.amount.stringRepresentation} (${it.send.amount.origin.name}) RECEIVE:${it.receive.amount.amount.stringRepresentation} (${it.receive.amount.origin.name}) DURATION:${if (it.simulation.uptimeMillis == 0L) "NONE" else (SystemClock.uptimeMillis() - it.simulation.uptimeMillis).toString() + "ms"}")
                }
            }
        }
    }

    fun swapTokens(includeAmount: Boolean = true) {
        _swapState.updateIf(SwapState::canBeModified) { state ->
            val newSend: SwapEntity
            val newReceive: SwapEntity
            if (state.send.amount.isUserInput || !state.receive.amount.isUserInput) {
                // Preserve SEND amount, because it is provided by user
                // Update RECEIVE with up-to-date rate
                if (includeAmount) {
                    newSend = state.receive.withAmountOrigin(SwapAmount.Origin.STALE_INPUT)
                    newReceive = state.send
                } else {
                    newSend = state.receive.withAmount(state.send.amount)
                    newReceive = state.send.withAmount(state.receive.amount.withOrigin(SwapAmount.Origin.STALE_INPUT))
                }
            } else {
                // Preserve RECEIVE amount, because it is provided by user
                // Update SEND with up-to-date rate
                if (includeAmount) {
                    newSend = state.receive
                    newReceive = state.send.withAmountOrigin(SwapAmount.Origin.STALE_INPUT)
                } else {
                    newSend = state.receive.withAmount(state.send.amount.withOrigin(SwapAmount.Origin.STALE_INPUT))
                    newReceive = state.send.withAmount(state.receive.amount)
                }
            }
            state.copy(
                send = newSend,
                receive = newReceive,
            ).withoutContext()
        }
    }

    fun selectToken(target: SwapTarget, acquiredToken: AccountTokenEntity) {
        val asset = remoteAssets.value.map[acquiredToken.address]
        val entity = asset?.token ?: TokenEntity(
            acquiredToken.address,
            acquiredToken.name,
            acquiredToken.symbol,
            acquiredToken.imageUri,
            acquiredToken.decimals,
            TokenEntity.Verification.whitelist
        )
        selectToken(target, entity, asset)
    }

    fun selectToken(target: SwapTarget, asset: AssetEntity) {
        selectToken(target, asset.token, asset)
    }

    private fun selectToken(target: SwapTarget, token: TokenEntity?, asset: AssetEntity?) {
        val targetEntity = swapState.value.takeSwapEntity(target)
        if (token.isSameToken(targetEntity.token)) return

        val oppositeEntity = swapState.value.takeSwapEntity(target.reverse)

        if (token.isSameToken(oppositeEntity.token)) {
            swapTokens(false)
            return
        }

        val removeOtherToken = if (token != null && oppositeEntity.token != null) {
            val address = token.getUserFriendlyAddress(false, false)
            val otherAddress = oppositeEntity.token.getUserFriendlyAddress(false, false)
            !marketList.value[otherAddress].contains(address)
        } else false

        _swapState.updateIf(SwapState::canBeModified) { state ->
            val oldEntity = state.takeSwapEntity(target)
            val newEntity = oldEntity.copy(
                token = token,
                amount = if (oldEntity.amount.isUserInput) {
                    oldEntity.amount
                } else {
                    oldEntity.amount.withOrigin(SwapAmount.Origin.STALE_INPUT)
                },
                asset = null, displayData = null
            ).withRemoteDetails(asset, null)

            val otherEntity = state.takeSwapEntity(target.reverse)
            val newOtherEntity = if (removeOtherToken) {
                SwapEntity.EMPTY
            } else if (newEntity.amount.isUserInput || !otherEntity.amount.isUserInput) {
                otherEntity.withAmountOrigin(SwapAmount.Origin.STALE_INPUT)
            } else {
                otherEntity
            }

            val result = when (target) {
                SwapTarget.SEND -> state.copy(
                    send = newEntity,
                    receive = newOtherEntity
                )
                SwapTarget.RECEIVE -> state.copy(
                    receive = newEntity,
                    send = newOtherEntity
                )
            }

            result.withoutContext()
        }
    }

    fun setAmount(target: SwapTarget, amount: FormattedDecimal, origin: SwapAmount.Origin = SwapAmount.Origin.ACTIVE_USER_INPUT) = _swapState.updateIf(SwapState::canBeModified) { state ->
        val prevEntity = state.takeSwapEntity(target)
        if (prevEntity.amount.origin == origin && prevEntity.amount.amount == amount) {
            state
        } else {
            val newAmount = SwapAmount(amount, origin)
            val newEntity = prevEntity.withAmount(newAmount)

            val prevOtherEntity = state.takeSwapEntity(target.reverse)
            var newOtherAmount: SwapAmount = SwapAmount.EMPTY
            if (!newAmount.isEmpty && newAmount.isUserInput) {
                newOtherAmount = prevOtherEntity.amount.withOrigin(SwapAmount.Origin.STALE_INPUT)
                if (SwapConfig.APP_SIMULATION_GUESSES_ENABLED && state.send.hasToken && state.receive.hasToken) {
                    val amountGuess = state.findLastSuccessfulSimulation(state.send.token!!, state.receive.token!!)?.guessAmountOrNull(
                        prevOtherEntity.token!!,
                        newEntity.token!!,
                        newEntity.amount.amount.number
                    )
                    amountGuess?.let {
                        newOtherAmount = SwapAmount(
                            FormattedDecimal(it, it.toDefaultCoinAmount(prevOtherEntity.token)),
                            SwapAmount.Origin.LOCAL_SIMULATION_GUESS
                        )
                    }
                }
            }
            val newOtherEntity = prevOtherEntity.withAmount(newOtherAmount)
            val resetContext = newAmount.isUserInput && newAmount.isEmpty
            val result = when (target) {
                SwapTarget.RECEIVE -> state.copy(
                    receive = newEntity,
                    send = newOtherEntity
                )
                SwapTarget.SEND -> state.copy(
                    send = newEntity,
                    receive = newOtherEntity
                )
            }
            if (resetContext) {
                result.withoutContext()
            } else {
                result
            }
        }
    }

    fun setSettings(newSettings: SwapSettings) =
        _swapState.updateIf(SwapState::canBeModified) { state ->
            if (LOGGING_ENABLED) {
                Log.v(LOGGING_TAG, "[${debugTimestamp()}] applying settings: $newSettings, canRunSimulations:${state.canRunSimulations}")
            }
            if (state.settings.slippageTolerance != newSettings.slippageTolerance) {
                state.copy(
                    settings = newSettings,
                    // visibleSimulation = null
                ).withoutContext()
            } else {
                state.copy(
                    settings = newSettings
                )
            }
        }

    private suspend fun loadData() = withContext(Dispatchers.IO) {
        loadTokens()
        loadAssets()
        loadMarketList()
    }

    suspend fun getWalletCurrency() = withContext(Dispatchers.IO) {
        settingsRepository.currencyFlow.firstOrNull() ?: WalletCurrency.DEFAULT
    }

    private suspend fun loadTokens() = withContext(Dispatchers.IO) {
        val wallet = walletManager.getWalletInfo()!!
        val accountId = wallet.accountId
        val walletCurrency = getWalletCurrency()
        val tokens = tokenRepository.get(walletCurrency, accountId, wallet.testnet)
        _accountTokens.tryEmit(tokens)

        if (tokens.isNotEmpty()) {
            _swapState.update { state ->
                val filtered = tokens.filter { token ->
                    (state.send.token?.isSameToken(token) ?: false) ||
                    (state.receive.token?.isSameToken(token) ?: false)
                }
                if (filtered.isEmpty()) {
                    return@update state
                }

                val sendAccountToken = state.send.token?.let { token ->
                    filtered.firstOrNull { accountToken -> token.isSameToken(accountToken) }
                }
                val receiveAccountToken = state.receive.token?.let { token ->
                    filtered.firstOrNull { accountToken -> token.isSameToken(accountToken) }
                }

                val updatedSend = state.send.takeIf { it.asset == null }?.let { entity ->
                    sendAccountToken?.let { accountToken ->
                        SwapEntity(
                            entity.token,
                            entity.asset,
                            entity.amount,
                            accountToken
                        )
                    } ?: entity
                } ?: state.send
                val updatedReceive = state.receive.takeIf { it.asset == null}?.let { entity ->
                    receiveAccountToken?.let { accountToken ->
                        SwapEntity(
                            entity.token,
                            entity.asset,
                            entity.amount,
                            accountToken
                        )
                    } ?: entity
                } ?: state.receive

                if (state.send != updatedSend || state.receive != updatedReceive) {
                    state.copy(
                        send = updatedSend,
                        receive = updatedReceive
                    )
                } else {
                    state
                }
            }
        }
    }

    private suspend fun loadMarketList() = withContext(Dispatchers.IO) {
        val marketList = this.let {
            var res: JSONArray? = null
            do {
                try {
                    res = tokenRepository.loadMarketList()
                } catch (e: Exception) {
                    if (LOGGING_ENABLED) {
                        Log.e(LOGGING_TAG, "Unable to load market list", e)
                    }
                    networkMonitor.isOnlineFlow.first { it }
                    delay(TRY_AGAIN_TIMEOUT)
                }
            } while (res == null)
            res
        }

        val pairs: MutableList<Pair<String, String>> = mutableListOf()
        for (index in 0 until marketList.length()) {
            val array = marketList.getJSONArray(index)
            val fromAddress = array.getString(0)
            val toAddress = array.getString(1)
            pairs += Pair(fromAddress, toAddress)
        }
        val map = PairMap(pairs)

        if (map.isNotEmpty()) {
            _marketList.tryEmit(map)
        }
    }

    private suspend fun loadRates(tokens: List<String>): RatesEntity? = withContext(Dispatchers.IO) {
        try {
            val walletCurrency = settingsRepository.currencyFlow.firstOrNull()
            walletCurrency?.takeIf {
                SwapConfig.FORCE_CHECK_USD_RATE || it.code != WalletCurrency.USD_CODE
            }?.let {
                ratesRepository.getRates(it, tokens)
            }
        } catch (e: Exception) {
            if (LOGGING_ENABLED) {
                Log.w(LOGGING_TAG, "Unable to check user rates", e)
            }
            null
        }
    }

    private suspend fun loadAssets() = withContext(Dispatchers.IO) {
        val wallet = walletManager.getWalletInfo()!!
        val rawAssets = this.let {
            var res: JSONArray? = null
            do {
                try {
                    res = tokenRepository.loadRawAssets(wallet.address, wallet.testnet)
                } catch (e: Exception) {
                    if (LOGGING_ENABLED) {
                        Log.w(LOGGING_TAG, "Unable to load assets", e);
                    }
                    networkMonitor.isOnlineFlow.first { it }
                    delay(TRY_AGAIN_TIMEOUT)
                }
            } while (res == null)
            res
        }

        val assetsList = mutableListOf<AssetEntity>()
        for (index in 0 until rawAssets.length()) {
            val assetJson = rawAssets.getJSONObject(index)
            val asset = try {
                assetJson.toAssetEntity()
            } catch (e: Exception) {
                continue
            }
            assetsList.add(asset)
        }

        // TODO(API): avoid extra request by providing user currency in response
        updateRates(assetsList)

        assetsList.sort()
        if (assetsList.isNotEmpty()) {
            val assets = RemoteAssets(assetsList)
            _remoteAssets.tryEmit(assets)

            _swapState.update { state ->
                if (state.remoteAssets != null) {
                    return@update state
                }
                val sendAsset = state.send.token?.let { assets.findAsset(it) }
                val receiveAsset = state.receive.token?.let { assets.findAsset(it) }

                val newSend = sendAsset?.let { state.send.withRemoteDetails(it, null) } ?: state.send
                val newReceive = receiveAsset?.let { state.receive.withRemoteDetails(it, null) } ?: state.receive

                state.copy(
                    remoteAssets = assets,
                    send = newSend,
                    receive = newReceive
                )
            }
        }
    }

    private suspend fun updateRates(assetsList: MutableList<AssetEntity>): Boolean {
        if (assetsList.isEmpty()) return false
        val tokensList = assetsList.filter { it.hasFunds }.map { it.token.address }.toSet().toList()
        val rates = loadRates(tokensList)
        if (rates == null || rates.isEmpty) return false
        var updatesCount = 0
        for (index in 0 until assetsList.size) {
            val asset = assetsList[index]
            if (asset.balance == null) continue
            val rate = rates.rate(asset.token.address)
            if (rate != null) {
                assetsList[index] = asset.withRate(rate)
                updatesCount++
            }
        }
        return updatesCount > 0
    }

    private var lastSimulationRequestMillis: Long = 0

    private val _requestsCount = MutableStateFlow(0)
    val requestsCount: StateFlow<Int> = _requestsCount

    private suspend fun simulateSwap() = withContext(Dispatchers.IO) {
        val state = swapState.first { state ->
            state.canRunSimulations
        }

        _requestsCount.value++

        val now = SystemClock.uptimeMillis()
        val elapsedSincePreviousRequest = if (lastSimulationRequestMillis > 0L) {
            now - lastSimulationRequestMillis
        } else {
            -1
        }
        lastSimulationRequestMillis = now
        val wallet = walletManager.getWalletInfo()!!
        val send = state.send
        val sendAddress = send.token!!.getUserFriendlyAddress(false, wallet.testnet)
        val receive = state.receive
        val receiveAddress = receive.token!!.getUserFriendlyAddress(false, wallet.testnet)
        val isReverse: Boolean
        val amount: SwapAmount
        val decimals: Int
        if (state.prioritizeSendEntity) {
            isReverse = false
            amount = send.amount
            decimals = send.token.decimals
        } else {
            isReverse = true
            amount = receive.amount
            decimals = receive.token.decimals
        }

        val units = Coin.toNanoString(amount.amount.number, decimals)
        val slippageTolerance = state.settings.slippageTolerance.toPlainString()

        if (LOGGING_ENABLED) {
            Log.v(LOGGING_TAG, "[${debugTimestamp()}] simulating swap... slippage:${slippageTolerance} elapsed: ${elapsedSincePreviousRequest}ms ${if (isReverse) "ASKING" else "OFFERING"}: $units SLIPPAGE: $slippageTolerance")
        }

        val result = try {
            val rawResult = tokenRepository.simulateSwap(
                sendAddress,
                receiveAddress,
                units,
                isReverse,
                slippageTolerance
            )
            val simulation = rawResult.toSimulationEntity(send.token, receive.token, isReverse)
            val result = SimulationResult(send, receive, isReverse, simulation)
            if (LOGGING_ENABLED) {
                Log.v(LOGGING_TAG, "[${debugTimestamp()}] simulation finished OFFER:${simulation.send.coins.toPlainString()} RECEIVE:${simulation.receive.coins.toPlainString()} SWAP_RATE:${simulation.swapRate.toPlainString()} RAW:${rawResult.toString(2)}")
            }
            result
        } catch (error: Exception) {
            if (LOGGING_ENABLED) {
                Log.e(LOGGING_TAG, "[${debugTimestamp()}] simulation failed: ${error.message}")
            }
            SimulationResult(send, receive, isReverse, error = error)
        }

        _requestsCount.value--
        _swapState.update {
            it.updateWithSimulation(result)
        }
    }

    suspend fun requestSwapConfirmation(): SwapRequest? = withContext(Dispatchers.IO) {
        val initialState = swapState.firstOrNull { state ->
            state.status == SwapState.Status.READY_FOR_USER_CONFIRMATION && !state.requiresSimulationUpdate
        }
        if (initialState == null) {
            return@withContext null
        }
        _swapState.update { state ->
            if (state.status != SwapState.Status.READY_FOR_USER_CONFIRMATION) {
                return@update state
            }

            val send = state.takeSwapEntity(SwapTarget.SEND)
            val receive = state.takeSwapEntity(SwapTarget.RECEIVE)
            require(state.simulation.data != null && state.visibleSimulation != null)
            val userRequest = SwapRequest(
                sourceWallet = state.walletAddress,
                send = send,
                receive = receive,
                prioritizeSendAmount = state.prioritizeSendEntity,
                confirmedSimulation = SwapSimulation(state.simulation, state.visibleSimulation)
            )

            state.copy(
                feesRequest = userRequest
            )
        }

        val originalState = swapState.first { state ->
            state.feesRequest?.isRequestPending ?: false
        }
        val originalRequest = originalState.feesRequest!!
        var operationDetails = simulateOnBlockchain(originalRequest)
        if (operationDetails.canAttempt && !operationDetails.hasError) {
            val transferDetails = operationDetails.transferDetails!!
            operationDetails = operationDetails.let { success ->
                try {
                    // Check #1: gas. Currently only for TON
                    val remainingBalance = if (originalRequest.send.isTon) {
                        originalRequest.send.balance - Coin.toCoins(transferDetails.attachedAmount)
                    } else BigDecimal.ZERO
                    if (remainingBalance < BigDecimal.ZERO) {
                        error("Low balance")
                    }
                } catch (balanceLowError: Exception) {
                    return@let success.copy(
                        error = SwapOperationError.LOW_BALANCE_MAY_FAIL,
                        errorMessage = originalRequest.sendAsset.token.symbol
                    )
                }
                return@let success
            }
        }

        val newVisibleSimulation = operationDetails.feesInTon?.let { feesInTon ->
            originalRequest.confirmedSimulation.visibleSimulation.copy(
                blockchainFee = "≈ ${feesInTon.stringRepresentation} ${TokenEntity.TON.symbol}"
            )
        } ?: originalState.visibleSimulation

        var newSend = originalState.send
        var newReceive = originalState.receive

        try {
            // Check #2: check latest currency rate for clarity.
            val sendAsset = originalRequest.sendAsset
            val receiveAsset = originalRequest.receiveAsset
            val sendAddress = sendAsset.token.address
            val receiveAddress = receiveAsset.token.address
            val rates = loadRates(listOf(
                sendAddress,
                receiveAddress
            ))

            val sendRate = rates?.rate(sendAddress)
            val receiveRate = rates?.rate(receiveAddress)

            if (sendRate == null && receiveRate == null) {
                error("${newSend.asset!!.token.symbol} and ${newReceive.asset!!.token.symbol} rates are unknown")
            }
            if (sendRate == null || receiveRate == null) {
                error("${(newSend.takeIf { sendRate == null } ?: newReceive).asset!!.token.symbol} rate is unknown")
            }
            // Update only if we received both.
            newSend = newSend.withRemoteDetails(sendAsset.withRate(sendRate), null)
            newReceive = newReceive.withRemoteDetails(receiveAsset.withRate(receiveRate), null)

            var newSendDisplayData = newSend.displayData
            var newReceiveDisplayData = newReceive.displayData
            val currency = sendAsset.userCurrency
            if (newSendDisplayData != null && newReceiveDisplayData != null) {
                if (currency != null && (currency == receiveAsset.userCurrency || currency.code == receiveAsset.userCurrency?.code)) {
                    // Increase fractional part until it differs (user currency)
                    val aFormat = newSendDisplayData.amountInUserCurrency
                    val bFormat = newReceiveDisplayData.amountInUserCurrency
                    if (aFormat == bFormat && aFormat.isNotEmpty()) {
                        val aPrice = sendAsset.anyPriceInUserCurrency
                        val bPrice = receiveAsset.anyPriceInUserCurrency
                        val a = aPrice?.let { it * newSend.amount.amount.number }?.stripTrailingZeros()
                        val b = bPrice?.let { it * newReceive.amount.amount.number }?.stripTrailingZeros()
                        if (a != null && b != null && a != b) {
                            val pair = a.createPair(currency, b, SwapConfig.MAX_INCREASE_FRACTIONAL_PART)
                            if (pair != null) {
                                newSendDisplayData = newSendDisplayData.copy(
                                    amountInUserCurrency = pair.first
                                )
                                newSend = newSend.copy(
                                    minDecimalsInUserCurrency = pair.decimals,
                                    displayData = newSendDisplayData
                                )
                                newReceiveDisplayData = newReceiveDisplayData.copy(
                                    amountInUserCurrency = pair.second
                                )
                                newReceive = newReceive.copy(
                                    minDecimalsInUserCurrency = pair.decimals,
                                    displayData = newReceiveDisplayData
                                )
                            }
                        }
                    }
                }
                if (newSendDisplayData.amountInUsd == newReceiveDisplayData.amountInUsd) {
                    // Increase fractional part until it differs (USD)
                    val aFormat = newSendDisplayData.amountInUsd
                    val bFormat = newReceiveDisplayData.amountInUsd
                    if (aFormat.isNotEmpty()) {
                        val aPrice = sendAsset.anyPriceInUsd
                        val bPrice = receiveAsset.anyPriceInUsd
                        val a = aPrice?.let { it * newSend.amount.amount.number }?.stripTrailingZeros()
                        val b = bPrice?.let { it * newReceive.amount.amount.number }?.stripTrailingZeros()
                        if (a != null && b != null && a != b) {
                            val pair = a.createPair(WalletCurrency.USD, b, SwapConfig.MAX_INCREASE_FRACTIONAL_PART)
                            if (pair != null) {
                                newSendDisplayData = newSendDisplayData.copy(
                                    amountInUsd = pair.first
                                )
                                newSend = newSend.copy(
                                    minDecimalsInUsd = pair.decimals,
                                    displayData = newSendDisplayData
                                )
                                newReceiveDisplayData = newReceiveDisplayData.copy(
                                    amountInUsd = pair.second
                                )
                                newReceive = newReceive.copy(
                                    minDecimalsInUsd = pair.decimals,
                                    displayData = newReceiveDisplayData
                                )
                            }
                        }
                    }
                }
            }
        } catch (currencyRateCheckError: Exception) {
            operationDetails = operationDetails.copy(
                error = SwapOperationError.CURRENCY_RATE_UPDATE_FAILED,
                errorMessage = currencyRateCheckError.toUserVisibleMessage()
            )
        }

        val result = originalRequest.copy(
            send = newSend,
            receive =  newReceive,
            operationDetails = operationDetails,
            confirmedSimulation = if (newVisibleSimulation != null) {
                originalRequest.confirmedSimulation.copy(
                    visibleSimulation = newVisibleSimulation
                )
            } else {
                originalRequest.confirmedSimulation
            }
        )

        _swapState.update { state ->
            if (state.feesRequest == originalRequest) {
                state.copy(
                    feesRequest = result
                )
            } else {
                state
            }
        }

        return@withContext result
    }

    private suspend fun getStateInit(wallet: WalletEntity, knownSeqno: Int = -1): StateInit? = withContext(Dispatchers.IO) {
        val seqno = if (knownSeqno == -1) {
            walletRepository.walletSeqno(wallet)
        } else knownSeqno
        if (SwapConfig.NEED_STATE_INIT && seqno == 0) {
            // val walletLegacy = walletManager.getWalletInfo()!!
            // walletLegacy.stateInit
            wallet.contract.stateInit
        } else {
            null
        }
    }

    private suspend fun simulateOnBlockchain(userRequest: SwapRequest): SwapOperationDetails = withContext(Dispatchers.IO) {
        try {
            val wallet = walletRepository.getWalletByAddress(userRequest.sourceWallet)!!
            val transferDetails = userRequest.prepareSwapTransferDetails(walletRepository, wallet)
            val stateInit: StateInit? = getStateInit(wallet)

            val walletTransfer = transferDetails.toWalletTransfer(stateInit)
            val cell = walletRepository.createSignedMessage(wallet, EmptyPrivateKeyEd25519, SwapTimeout.generateValidUntilSeconds(), listOf(walletTransfer))

            val consequences = api.emulate(cell, wallet.testnet)
            val feesInTonUnits = consequences.totalFees
            val feesInTonCoins = Coin.toCoins(feesInTonUnits.toBigInteger() + transferDetails.userVisibleFeesAmount)
            val feesInTon = FormattedDecimal(feesInTonCoins, feesInTonCoins.toDisplayAmount(App.defaultNumberFormat(), Coin.TON_DECIMALS))

            val success = SwapOperationDetails(
                transferDetails = transferDetails,
                feesInTon = feesInTon
            )

            return@withContext success
        } catch (e: Exception) {
            return@withContext SwapOperationDetails(
                error = SwapOperationError.SIMULATION_FAILED,
                errorMessage = e.toUserVisibleMessage()
            )
        }
    }

    /**
     * Returns SwapTransfer in WAITING_FOR_SIGNATURE/FAILED state, or null.
     *
     * App should call
     */
    suspend fun startSwap(context: Context, minDelayBeforePasscode: Long): SwapTransfer? = withContext(Dispatchers.IO) {
        val startTimeMs = SystemClock.uptimeMillis()
        val state = _swapState.firstOrNull {
            when (it.status) {
                SwapState.Status.SWAP_READY,
                SwapState.Status.SWAP_FAILED -> true
                else -> false
            }
        } ?: return@withContext null

        val originalTransfer = state.transfer!!
        if (!originalTransfer.canPerformOnBlockchain) return@withContext null
        val userRequest = originalTransfer.request

        val preparing = originalTransfer.copy(
            state = SwapTransfer.State.PREPARING,

            signerInput = null,
            error = null,
            success = null
        )
        _swapState.update {
            if (it.transfer == originalTransfer) {
                it.copy(transfer = preparing)
            } else {
                it
            }
        }

        try {
            val wallet = walletRepository.getWalletByAddress(userRequest.sourceWallet)!!
            val seqno = walletRepository.walletSeqno(wallet)
            val stateInit: StateInit? = getStateInit(wallet, seqno)

            val transferDetails = userRequest.operationDetails!!.transferDetails!!
            val walletTransfer = transferDetails.toWalletTransfer(stateInit)

            val walletLegacy = walletManager.getWalletInfo()!!
            if (walletLegacy.signer) {
                val input = SingerResultContract.Input(walletTransfer.body!!, wallet.publicKey)
                val waitingForSignature = preparing.copy(
                    state = SwapTransfer.State.WAITING_FOR_SIGNATURE,
                    seqno = seqno,
                    signerInput = input
                )
                updateTransfer(preparing, waitingForSignature)
                return@withContext waitingForSignature
            }

            val elapsedBeforePasscode = SystemClock.uptimeMillis() - startTimeMs
            if (elapsedBeforePasscode < minDelayBeforePasscode) {
                // Delay to complete cancel button animation
                delay(minDelayBeforePasscode - elapsedBeforePasscode)
            }

            if (!passcodeRepository.confirmation(context)) {
                val failedTransfer = failTransfer(preparing, "Passcode unconfirmed")
                return@withContext failedTransfer
            }

            val inProgress = preparing.copy(
                state = SwapTransfer.State.IN_PROGRESS
            )
            updateTransfer(preparing, inProgress)

            try {
                val privateKey = walletManager.getPrivateKey(wallet.id)
                val resultCell = walletLegacy.sendToBlockchain(api, privateKey, walletTransfer)
                    ?: throw Exception("failed to send to blockchain")
                val success = inProgress.copy(
                    state = SwapTransfer.State.SUCCESSFUL,
                    success = resultCell
                )
                updateTransfer(inProgress, success)
                return@withContext success
            } catch (e: Exception) {
                return@withContext failTransfer(inProgress, "failed to send to blockchain")
            }
        } catch (e: Exception) {
            val failedTransfer = originalTransfer.copy(
                state = SwapTransfer.State.FAILED,
                error = e.toUserVisibleMessage()
            )
            _swapState.update {
                if (it.transfer == preparing) {
                    it.copy(
                        transfer = failedTransfer
                    )
                } else {
                    it
                }
            }
            return@withContext failedTransfer
        }
    }

    suspend fun failCurrentTransfer(error: String) {
        val state = swapState.firstOrNull {
            it.transfer?.state?.inProgress ?: false
        } ?: return
        failTransfer(state.transfer!!, error)
    }

    fun failTransfer(originalTransfer: SwapTransfer, error: String): SwapTransfer {
        val failedTransfer = originalTransfer.copy(
            signerInput = null,
            error = error,
            state = SwapTransfer.State.FAILED
        )
        updateTransfer(originalTransfer, failedTransfer)
        return failedTransfer
    }

    private fun updateTransfer(originalTransfer: SwapTransfer, updatedTransfer: SwapTransfer) {
        _swapState.update { state ->
            if (state.transfer == originalTransfer) {
                state.copy(transfer = updatedTransfer)
            } else {
                state
            }
        }
    }

    suspend fun continueSwap(data: ByteArray?): SwapTransfer? = withContext(Dispatchers.IO) {
        val state = swapState.firstOrNull {
            it.transfer?.state == SwapTransfer.State.WAITING_FOR_SIGNATURE
        } ?: return@withContext null
        val waitingForSignature = state.transfer!!

        if (data == null) {
            return@withContext failTransfer(waitingForSignature, "Cancelled by user")
        }
        val inProgress = waitingForSignature.copy(
            state = SwapTransfer.State.IN_PROGRESS
        )
        updateTransfer(waitingForSignature, inProgress)
        try {
            val wallet = walletManager.getWalletInfo()!!
            val contract = wallet.contract
            val seqno = waitingForSignature.seqno
            val unsignedBody = waitingForSignature.signerInput!!.body

            val signature = BitString(data)
            val signerBody = contract.signedBody(signature, unsignedBody)
            val cell = contract.createTransferMessageCell(wallet.contract.address, seqno, signerBody)

            if (!wallet.sendToBlockchain(api, cell)) {
                return@withContext failTransfer(inProgress, "failed")
            }

            val success = waitingForSignature.copy(
                state = SwapTransfer.State.SUCCESSFUL,
                signerInput = null,
                seqno = -1,
                success = cell
            )
            updateTransfer(inProgress, success)
            return@withContext success
        } catch (e: Exception) {
            return@withContext failTransfer(inProgress, e.message ?: "Continue swap failed")
        }
    }
}