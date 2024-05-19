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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.math.BigDecimal

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
    private val exchangeRate = combine(pickedSendAsset, pickedReceiveAsset) { toSend, toReceive ->
        when {
            toSend == null -> null
            toReceive == null -> null
            else -> toSend to toReceive
        }
    }.filterNotNull()
        .flatMapLatest { (toSend, toReceive) ->
            getRateFlowCase.execute(
                initialTokens = listOf(
                    toSend.contractAddress,
                    toReceive.contractAddress
                )
            ).map { Triple(it, toSend, toReceive) }
        }
    val receiveAmount = combine(
        sendAmount,
        exchangeRate,
    ) { amount, (rate, toSend, toReceive) ->
        val sendRate = rate.rate(toSend.contractAddress)!!
        val receiveRate = rate.rate(toReceive.contractAddress)!!
        val sendAmount = sendRate.value * amount
        val receiveAmount = sendAmount / receiveRate.value
        receiveAmount to toReceive.symbol
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

    fun onSwapTokensClicked() {
        val toSend = _pickedSendAsset.value
        _pickedSendAsset.value = _pickedReceiveAsset.value
        _pickedReceiveAsset.value = toSend
    }

    fun onSendAmountChanged(amount: BigDecimal) {
        if (sendAmount.value == amount) return
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