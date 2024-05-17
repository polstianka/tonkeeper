package com.tonapps.tonkeeper.fragment.swap.root

import android.util.Log
import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.core.emit
import com.tonapps.tonkeeper.core.observeFlow
import com.tonapps.tonkeeper.fragment.swap.domain.DexAssetsRepository
import com.tonapps.tonkeeper.fragment.swap.domain.GetDefaultSwapSettingsCase
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAsset
import com.tonapps.tonkeeper.fragment.swap.pick_asset.PickAssetResult
import com.tonapps.tonkeeper.fragment.swap.pick_asset.PickAssetType
import com.tonapps.tonkeeper.fragment.swap.settings.SwapSettingsResult
import com.tonapps.wallet.data.account.WalletRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class SwapViewModel(
    private val repository: DexAssetsRepository,
    walletManager: WalletRepository,
    getDefaultSwapSettingsCase: GetDefaultSwapSettingsCase
) : ViewModel() {

    private val swapSettings = MutableStateFlow(getDefaultSwapSettingsCase.execute())
    private val _pickedSendAsset = MutableStateFlow<DexAsset?>(null)
    private val _pickedReceiveAsset = MutableStateFlow<DexAsset?>(null)
    private val _events = MutableSharedFlow<SwapEvent>()
    private val activeWallet = walletManager.activeWalletFlow
    private val pairFlow = combine(activeWallet, _pickedSendAsset) { a, b -> a to b }

    val isLoading = repository.isLoading
    val events: Flow<SwapEvent>
        get() = _events
    val pickedSendAsset: Flow<DexAsset?>
        get() = _pickedSendAsset
    val pickedReceiveAsset: Flow<DexAsset?>
        get() = _pickedReceiveAsset
    val pickedTokenBalance = pairFlow.flatMapLatest { pair ->
        val asset = pair.second
        if (asset == null) {
            flowOf(null)
        } else {
            repository.getAssetBalance(pair.first.address, asset.contractAddress)
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

    fun onSwapTokensClicked() {
        val toSend = _pickedSendAsset.value
        _pickedSendAsset.value = _pickedReceiveAsset.value
        _pickedReceiveAsset.value = toSend
    }

    fun onSendAmountChanged(amount: Float) {
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