package com.tonapps.tonkeeper.ui.screen.swapnative.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.Coin
import com.tonapps.network.NetworkMonitor
import com.tonapps.security.hex
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.extensions.getSeqno
import com.tonapps.tonkeeper.extensions.sendToBlockchain
import com.tonapps.tonkeeper.ui.screen.swapnative.confirm.SwapConfirmArgs
import com.tonapps.tonkeeper.ui.screen.swapnative.SwapData
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.account.legacy.WalletLegacy
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.AssetRepository
import com.tonapps.wallet.data.token.SwapRepository
import com.tonapps.wallet.data.token.TokenRepository
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import com.tonapps.wallet.data.token.entities.AssetEntity
import com.tonapps.wallet.data.token.entities.SwapSimulateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ton.api.pub.PublicKeyEd25519
import org.ton.bitstring.BitString
import org.ton.block.StateInit
import org.ton.cell.Cell
import ton.transfer.STONFI_CONSTANTS

class SwapNativeViewModel(
    private val assetRepository: AssetRepository,
    private val swapRepository: SwapRepository,
    private val networkMonitor: NetworkMonitor,
    private val walletRepository: WalletRepository,
    private val settings: SettingsRepository,
    private val tokenRepository: TokenRepository,
    private val api: API,
) : ViewModel() {

    private var _selectedFromToken = MutableStateFlow<AssetEntity?>(null)
    var selectedFromToken: StateFlow<AssetEntity?> = _selectedFromToken

    private var _selectedToToken = MutableStateFlow<AssetEntity?>(null)
    var selectedToToken: StateFlow<AssetEntity?> = _selectedToToken

    private var _swapDetailsFlow = MutableStateFlow<SwapSimulateEntity?>(null)
    var swapDetailsFlow: StateFlow<SwapSimulateEntity?> = _swapDetailsFlow

    private var _selectedFromTokenAmount = MutableStateFlow<String>("0")
    var selectedFromTokenAmount: StateFlow<String> = _selectedFromTokenAmount
    private var _selectedToTokenAmount = MutableStateFlow<String>("0")
    var selectedToTokenAmount: StateFlow<String> = _selectedToTokenAmount
    var isProgrammaticSet = false

    private val _tokenListFlow = MutableStateFlow<List<AccountTokenEntity>>(emptyList())
    val selectedSlippageFlow = MutableStateFlow<Float>(DEFAULT_SLIPPAGE)

    // confirm swap
    var walletEntity: WalletEntity? = null
    var walletAddress: String? = null

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

        }.launchIn(viewModelScope)

    }

    var periodicSimulateJob: Job? = null
    private fun startPeriodicSimulate(isReverse: Boolean) {
        periodicSimulateJob?.cancel()
        periodicSimulateJob = viewModelScope.launch {
            while (true) {
                delay(REPEAT_SIMULATE_INTERVAL)
                triggerSimulateSwap(isReverse)
            }
        }
    }

    private fun stopPeriodicSimulate() {
        periodicSimulateJob?.cancel()
    }

    fun setSelectedSellToken(sellAssetEntity: AssetEntity?) {

        viewModelScope.launch {
            withContext(Dispatchers.Default) {

                val accountTokenList = _tokenListFlow.value

                sellAssetEntity?.hiddenBalance = settings.hiddenBalances

                accountTokenList.forEach { accountTokenEntity ->
                    if (sellAssetEntity?.symbol == accountTokenEntity.symbol) {
                        sellAssetEntity.balance = accountTokenEntity.balance.value
                    }
                }

                _selectedFromToken.value = sellAssetEntity
            }
        }
    }

    fun setSelectedBuyToken(buyAssetEntity: AssetEntity?) {

        viewModelScope.launch {
            withContext(Dispatchers.Default) {

                val accountTokenList = _tokenListFlow.value

                val hiddenBalance = settings.hiddenBalances

                accountTokenList.forEach { accountTokenEntity ->
                    if (buyAssetEntity?.symbol == accountTokenEntity.symbol) {
                        buyAssetEntity.balance = accountTokenEntity.balance.value
                        buyAssetEntity.hiddenBalance = hiddenBalance
                    }
                }

                _swapDetailsFlow.value = null

                _selectedToToken.value = buyAssetEntity
            }
        }
    }

    private var onFromAmountChangedDebounceJob: Job? = null
    fun onFromAmountChanged(amount: String) {
        _selectedFromTokenAmount.value = amount

        onFromAmountChangedDebounceJob?.cancel()
        if (!isProgrammaticSet) {
            onFromAmountChangedDebounceJob = viewModelScope.launch {
                delay(1000)

                triggerSimulateSwap(false)
            }
        }
    }

    private var onToAmountChangedDebounceJob: Job? = null
    fun onToAmountChanged(amount: String) {
        _selectedToTokenAmount.value = amount

        onToAmountChangedDebounceJob?.cancel()
        if (!isProgrammaticSet) {
            onToAmountChangedDebounceJob = viewModelScope.launch {
                delay(1000)

                triggerSimulateSwap(true)
            }
        }
    }

    fun getRemoteAssets() {
        viewModelScope.launch {
            // pre fetch remote assets
            val assetMap = assetRepository.get(false)

            // put TON as initial selected asset
            if (selectedFromToken.value == null) {
                val ton = assetMap[AssetEntity.tonContractAddress]
                setSelectedSellToken(ton)
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

            stopPeriodicSimulate()

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
        viewModelScope.launch {
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
                startPeriodicSimulate(isReverse)

                // add decimals to this object
                swapDetails.fromDecimals = selectedFromToken.value?.decimals ?: 0
                swapDetails.toDecimals = selectedToToken.value?.decimals ?: 0
            }

            _swapDetailsFlow.value = swapDetails
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


    private var lastSeqno = -1
    private var lastUnsignedBody: Cell? = null
    private val _openSignerAppFlow = MutableSharedFlow<OpenSignerApp>()
    val openSignerAppFlow: SharedFlow<OpenSignerApp> = _openSignerAppFlow

    fun confirmSwap(
        swapType: SwapType
    ) {

        viewModelScope.launch(Dispatchers.IO) {
            val (jettonFromWalletAddress, jettonToWalletAddress) = when (swapType) {

                SwapType.JETTON_TO_JETTON -> {

                    val from = async {
                        swapRepository.getWalletAddress(
                            jettonMaster = selectedFromToken.value!!.contractAddress,
                            owner = walletAddress!!,
                            false
                        )
                    }

                    val to = async {
                        swapRepository.getWalletAddress(
                            jettonMaster = selectedToToken.value!!.contractAddress,
                            owner = STONFI_CONSTANTS.RouterAddress,
                            false
                        )
                    }

                    val fromresult = from.await()
                    val toresult = to.await()

                    (fromresult to toresult)
                }

                SwapType.JETTON_TO_TON -> {
                    val from = async {
                        swapRepository.getWalletAddress(
                            jettonMaster = selectedFromToken.value!!.contractAddress,
                            owner = walletAddress!!,
                            false
                        )
                    }

                    val to = async {
                        swapRepository.getWalletAddress(
                            jettonMaster = STONFI_CONSTANTS.TONProxyAddress,
                            owner = STONFI_CONSTANTS.RouterAddress,
                            false
                        )
                    }

                    val fromresult = from.await()
                    val toresult = to.await()

                    (fromresult to toresult)
                }

                SwapType.TON_TO_JETTON -> {
                    val from = async {
                        swapRepository.getWalletAddress(
                            jettonMaster = STONFI_CONSTANTS.TONProxyAddress,
                            owner = STONFI_CONSTANTS.RouterAddress,
                            false
                        )
                    }

                    val to = async {
                        swapRepository.getWalletAddress(
                            jettonMaster = selectedToToken.value!!.contractAddress,
                            owner = STONFI_CONSTANTS.RouterAddress,
                            false
                        )
                    }

                    val fromresult = from.await()
                    val toresult = to.await()

                    Log.d("swap-log", "#1 fromresult ${fromresult} toresult $toresult")

                    (fromresult to toresult)
                }
            }

            val (forwardAmount, attachedAmount) = when (swapType) {

                SwapType.JETTON_TO_JETTON -> {
                    (STONFI_CONSTANTS.SWAP_JETTON_TO_JETTON_ForwardGasAmount to
                            STONFI_CONSTANTS.SWAP_JETTON_TO_JETTON_GasAmount)
                }

                SwapType.JETTON_TO_TON -> {
                    (STONFI_CONSTANTS.SWAP_JETTON_TO_TON_ForwardGasAmount to
                            STONFI_CONSTANTS.SWAP_JETTON_TO_TON_GasAmount)
                }

                SwapType.TON_TO_JETTON -> {
                    (STONFI_CONSTANTS.SWAP_TON_TO_JETTON_ForwardGasAmount to
                            STONFI_CONSTANTS.SWAP_TON_TO_JETTON_ForwardGasAmount + swapDetailsFlow.value!!.offerUnits)
                }
            }

            // todo handle null !!
            sign(
                SwapData(
                    userWalletAddress = App.walletManager.getWalletInfo()!!.contract.address,
                    minAskAmount = swapDetailsFlow.value!!.minAskUnits,
                    offerAmount = swapDetailsFlow.value!!.offerUnits,
                    jettonFromWalletAddress = jettonFromWalletAddress!!,
                    jettonToWalletAddress = jettonToWalletAddress!!,
                    forwardAmount = forwardAmount,
                    attachedAmount = attachedAmount,
                    referralAddress = null
                )
            )
        }
    }

    private suspend fun sign(swapData: SwapData) {
        /*updateUiState {
            it.copy(
                processActive = true,
                processState = ProcessTaskView.State.LOADING
            )
        }
*/

        Log.d("swap-log", "#2 sign swapData ${swapData} ")

        val wallet = App.walletManager.getWalletInfo() ?: throw Exception("failed to get wallet")
        lastSeqno = getSeqno(wallet)
        lastUnsignedBody = buildUnsignedBody(wallet, lastSeqno, swapData)

        Log.d(
            "swap-log",
            "#7 sign lastUnsignedBody ${lastUnsignedBody}, wallet $wallet, lastseqno $lastSeqno "
        )

        _openSignerAppFlow.emit(OpenSignerApp(lastUnsignedBody!!, wallet.publicKey))
        // sendEffect(ConfirmScreenEffect.OpenSignerApp(lastUnsignedBody!!, wallet.publicKey))

    }

    private suspend fun buildUnsignedBody(
        wallet: WalletLegacy,
        seqno: Int,
        swapData: SwapData
    ): Cell {
        val validUntil = getValidUntil(wallet.testnet)
        val stateInit = getStateInitIfNeed(wallet)
        val transfer = swapData.buildSwapTransfer(wallet.contract.address, stateInit)

        Log.d(
            "swap-log",
            "#6 validUntil ${validUntil}, stateInit ${stateInit}, transfer $transfer "
        )

        return wallet.contract.createTransferUnsignedBody(
            validUntil,
            seqno = seqno,
            gifts = arrayOf(transfer)
        )
    }

    private suspend fun getValidUntil(testnet: Boolean): Long {
        val seconds = api.getServerTime(testnet)
        return seconds + 300L // 5 minutes
    }

    private suspend fun getStateInitIfNeed(wallet: WalletLegacy): StateInit? {
        if (0 >= lastSeqno) {
            lastSeqno = getSeqno(wallet)
        }
        if (lastSeqno == 0) {
            return wallet.contract.stateInit
        }
        return null
    }

    private suspend fun getSeqno(wallet: WalletLegacy): Int {
        if (0 >= lastSeqno) {
            lastSeqno = wallet.getSeqno(api)
        }
        return lastSeqno
    }

    fun sendSignature(data: ByteArray) {
        Log.d("swap-log", "# sendSignature: ${hex(data)}")
        /*updateUiState {
            it.copy(
                processActive = true,
                processState = ProcessTaskView.State.LOADING
            )
        }*/

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val wallet =
                    App.walletManager.getWalletInfo() ?: throw Exception("failed to get wallet")
                val contract = wallet.contract

                val unsignedBody = lastUnsignedBody ?: throw Exception("unsigned body is null")
                val signature = BitString(data)
                val signerBody = contract.signedBody(signature, unsignedBody)
                val b = contract.createTransferMessageCell(
                    wallet.contract.address,
                    lastSeqno,
                    signerBody
                )
                if (!wallet.sendToBlockchain(api, b)) {
                    throw Exception("failed to send to blockchain")
                }
                // successResult()
                Log.d("swap-log", "# sendSignature SUCCESS")
            } catch (e: Throwable) {
                Log.e("ConfirmScreenFeatureLog", "failed to send signature", e)
                // failedResult()
                Log.d("swap-log", "# sendSignature FAILED")
            }
        }
    }


    companion object {
        const val DEFAULT_SLIPPAGE = 0.1f

        const val REPEAT_SIMULATE_INTERVAL = 5000L
    }

    enum class SwapType {
        TON_TO_JETTON,
        JETTON_TO_JETTON,
        JETTON_TO_TON,
    }

    data class OpenSignerApp(
        val body: Cell,
        val publicKey: PublicKeyEd25519,
    )
}