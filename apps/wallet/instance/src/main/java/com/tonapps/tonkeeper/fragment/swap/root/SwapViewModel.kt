package com.tonapps.tonkeeper.fragment.swap.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.core.emit
import com.tonapps.tonkeeper.core.observeFlow
import com.tonapps.tonkeeper.fragment.swap.domain.DexAssetsRepository
import com.tonapps.tonkeeper.fragment.swap.domain.GetDefaultSwapSettingsCase
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAsset
import com.tonapps.tonkeeper.fragment.swap.pick_asset.PickAssetResult
import com.tonapps.tonkeeper.fragment.swap.pick_asset.PickAssetType
import com.tonapps.tonkeeper.fragment.swap.settings.SwapSettingsResult
import com.tonapps.tonkeeper.fragment.trade.domain.GetRateFlowCase
import com.tonapps.wallet.data.account.WalletRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalCoroutinesApi::class)
class SwapViewModel(
    private val repository: DexAssetsRepository,
    walletManager: WalletRepository,
    getDefaultSwapSettingsCase: GetDefaultSwapSettingsCase,
    getRateFlowCase: GetRateFlowCase
) : ViewModel() {

    private val swapSettings = MutableStateFlow(getDefaultSwapSettingsCase.execute())
    private val _pickedSendAsset = MutableStateFlow<DexAsset?>(null)
    private val _pickedReceiveAsset = MutableStateFlow<DexAsset?>(null)
    private val _events = MutableSharedFlow<SwapEvent>()
    private val activeWallet = walletManager.activeWalletFlow
    private val pairFlow = combine(activeWallet, _pickedSendAsset) { a, b -> a to b }
    private val sendAmount = MutableStateFlow(BigDecimal.ZERO)

    val isLoading = repository.isLoading
    val events: Flow<SwapEvent>
        get() = _events
    val pickedSendAsset: Flow<DexAsset?>
        get() = _pickedSendAsset
    val pickedReceiveAsset: Flow<DexAsset?>
        get() = _pickedReceiveAsset
    val pickedTokenBalance = pairFlow.flatMapLatest { (wallet, asset) ->
        if (asset == null) {
            flowOf(null)
        } else {
            repository.getAssetBalance(wallet.address, asset.contractAddress)
        }
    }
    val receiveAmount = combine(
        sendAmount,
        pickedSendAsset,
        pickedReceiveAsset
    ) { amount, sendAsset, receiveAsset  ->
        when {
            sendAsset == null -> null
            receiveAsset == null -> null
            else -> {
                val amount = amount * sendAsset.dexUsdPrice / receiveAsset.dexUsdPrice
                receiveAsset to amount
            }
        }
    }


    init {
        observeFlow(isLoading) { isLoading ->
            if (isLoading) return@observeFlow
            _pickedSendAsset.emit(repository.getDefaultAsset())
        }
    }

    fun onSettingsClicked() {
        val settings = swapSettings.value
        val event = SwapEvent.NavigateToSwapSettings(settings)
        emit(_events, event)
    }

    fun onCrossClicked() {
        emit(_events, SwapEvent.NavigateBack)
    }

    fun onSendTokenClicked() {
        val event = SwapEvent.NavigateToPickAsset(PickAssetType.SEND)
        emit(_events, event)
    }

    fun onReceiveTokenClicked() {
        val event = SwapEvent.NavigateToPickAsset(PickAssetType.RECEIVE)
        emit(_events, event)
    }

    fun onSwapTokensClicked() = viewModelScope.launch {
        val toSend = _pickedSendAsset.value
        val toReceiveAmount = receiveAmount.first()
        _pickedSendAsset.value = _pickedReceiveAsset.value
        _pickedReceiveAsset.value = toSend
        when (toReceiveAmount) {
            null -> Unit
            else -> {
                sendAmount.value = toReceiveAmount.second
                ignoreNextUpdate = true
                _events.emit(SwapEvent.FillInput(toReceiveAmount.second.setScale(2, RoundingMode.FLOOR).toPlainString()))
            }
        }
    }

    private var ignoreNextUpdate = false
    fun onSendAmountChanged(amount: BigDecimal) {
        if (sendAmount.value == amount) return
        if (ignoreNextUpdate) {
            ignoreNextUpdate = false
            return
        }
        sendAmount.value = amount
    }

    fun onAssetPicked(result: PickAssetResult) {
        when (result.type) {
            PickAssetType.SEND -> {
                _pickedSendAsset.value = result.asset
            }
            PickAssetType.RECEIVE -> {
                _pickedReceiveAsset.value = result.asset
            }
        }
    }

    fun onSettingsUpdated(result: SwapSettingsResult) {
        swapSettings.value = result.settings
    }
}