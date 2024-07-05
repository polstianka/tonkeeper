package com.tonapps.tonkeeper.ui.screen.swapnative.confirm

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.Coin
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.security.hex
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.extensions.getSeqno
import com.tonapps.tonkeeper.extensions.sendToBlockchain
import com.tonapps.tonkeeper.password.PasscodeRepository
import com.tonapps.tonkeeper.ui.screen.swapnative.SwapData
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.legacy.WalletLegacy
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.SwapRepository
import com.tonapps.wallet.data.token.entities.AssetEntity
import com.tonapps.wallet.data.token.entities.SwapSimulateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ton.bitstring.BitString
import org.ton.block.StateInit
import org.ton.cell.Cell
import ton.transfer.STONFI_CONSTANTS

class SwapConfirmViewModel(
    private val swapRepository: SwapRepository,
    private val passcodeRepository: PasscodeRepository,
    private val settingsRepository: SettingsRepository,
    private val api: API,
) : ViewModel() {

    lateinit var selectedFromToken: MutableStateFlow<AssetEntity>
    lateinit var selectedToToken: MutableStateFlow<AssetEntity>
    lateinit var swapDetailsFlow: MutableStateFlow<SwapSimulateEntity>

    var walletAddress: String? = null

    private var lastSeqno = -1
    private var lastUnsignedBody: Cell? = null

    private val _effectFlow = MutableSharedFlow<SwapConfirmScreenEffect>()
    val effectFlow: SharedFlow<SwapConfirmScreenEffect> = _effectFlow

    val screenStateFlow = MutableStateFlow(SwapConfirmScreenState.initState)

    var selectedCurrency: WalletCurrency? = null


    init {
        viewModelScope.launch {
            walletAddress = App.walletManager.getWalletInfo()?.accountId
                ?: throw Exception("failed to get wallet")
        }

        viewModelScope.launch {
            settingsRepository.currencyFlow.collect { walletCurrency ->
                selectedCurrency = walletCurrency
            }

        }

    }

    fun getfromAssetFiatInput(): String {
        return if (selectedFromToken.value != null && swapDetailsFlow.value != null) {
            val fromAsset = selectedFromToken.value
            val offerUnits = swapDetailsFlow.value.offerUnits

            var fiatBalance = (fromAsset.rate.toBigDecimal() * Coin.parseBigInt(
                offerUnits.toString(),
                fromAsset.decimals,
                false
            ))
            val fiatBalanceString = if (selectedCurrency != null) {
                CurrencyFormatter.format(selectedCurrency?.code!!, fiatBalance)
            } else fiatBalance.toString()
            fiatBalanceString.toString()
        } else ""
    }

    fun getToAssetFiatInput(): String {
        return if (selectedToToken.value != null && swapDetailsFlow.value != null) {
            val toAsset = selectedToToken.value
            val askUnits = swapDetailsFlow.value.askUnits

            var fiatBalance = (toAsset.rate.toBigDecimal() * Coin.parseBigInt(
                askUnits.toString(),
                toAsset.decimals,
                false
            ))
            val fiatBalanceString = if (selectedCurrency != null) {
                CurrencyFormatter.format(selectedCurrency?.code!!, fiatBalance)
            } else fiatBalance.toString()
            fiatBalanceString.toString()
        } else ""
    }

    fun decideSwapType(): SwapType? {
        return when {
            selectedFromToken.value.isTon && !selectedToToken.value.isTon -> SwapType.TON_TO_JETTON
            !selectedFromToken.value.isTon && selectedToToken.value.isTon -> SwapType.JETTON_TO_TON
            !selectedFromToken.value.isTon && !selectedToToken.value.isTon -> SwapType.JETTON_TO_JETTON
            else -> {
                null
            }
        }
    }

    fun confirmSwap(context: Context, swapType: SwapType) {

        screenStateFlow.update {
            it.copy(isLoading = true)
        }

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
            val swapData = SwapData(
                userWalletAddress = App.walletManager.getWalletInfo()!!.contract.address,
                minAskAmount = swapDetailsFlow.value!!.minAskUnits,
                offerAmount = swapDetailsFlow.value!!.offerUnits,
                jettonFromWalletAddress = jettonFromWalletAddress!!,
                jettonToWalletAddress = jettonToWalletAddress!!,
                forwardAmount = forwardAmount,
                attachedAmount = attachedAmount,
                referralAddress = null
            )

            val wallet = App.walletManager.getWalletInfo() ?: return@launch
            if (wallet.signer) {
                sign(swapData)
            } else {
                send(context, swapData)
            }

        }
    }

    private suspend fun send(context: Context, swapData: SwapData) {
//        updateUiState {
//            it.copy(
//                processActive = true,
//                processState = ProcessTaskView.State.LOADING
//            )
//        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val wallet =
                    App.walletManager.getWalletInfo() ?: throw Exception("failed to get wallet")
                if (!passcodeRepository.confirmation(context)) {
                    throw Exception("failed to request passcode")
                }
                val privateKey = App.walletManager.getPrivateKey(wallet.id)
                val gift =
                    swapData.buildSwapTransfer(wallet.contract.address, getStateInitIfNeed(wallet))
                val validUntil = getValidUntil(wallet.testnet)
                wallet.sendToBlockchain(validUntil, api, privateKey, gift)
                    ?: throw Exception("failed to send to blockchain")

                Log.d("swap-log", "# send to blockch SUCCESS")

                _effectFlow.emit(SwapConfirmScreenEffect.CloseScreen(true))
                screenStateFlow.update { it.copy(isLoading = false) }
            } catch (e: Throwable) {
                Log.d("swap-log", "# send to blockch fail")

                _effectFlow.emit(SwapConfirmScreenEffect.CloseScreen(false))
                screenStateFlow.update { it.copy(isLoading = false) }
            }
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

        _effectFlow.emit(
            SwapConfirmScreenEffect.OpenSignerApp(
                lastUnsignedBody!!,
                wallet.publicKey
            )
        )
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

                Log.d("swap-log", "# sendSignature SUCCESS")

                _effectFlow.emit(SwapConfirmScreenEffect.CloseScreen(true))
                screenStateFlow.update { it.copy(isLoading = false) }

            } catch (e: Throwable) {
                _effectFlow.emit(SwapConfirmScreenEffect.CloseScreen(false))
                screenStateFlow.update { it.copy(isLoading = false) }

                Log.e("ConfirmScreenFeatureLog", "failed to send signature", e)
                Log.d("swap-log", "# sendSignature FAILED")
            }
        }
    }

    enum class SwapType {
        TON_TO_JETTON,
        JETTON_TO_JETTON,
        JETTON_TO_TON,
    }


}