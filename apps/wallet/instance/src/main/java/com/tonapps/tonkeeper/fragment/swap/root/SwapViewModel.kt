package com.tonapps.tonkeeper.fragment.swap.root

import android.util.Log
import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.core.emit
import com.tonapps.tonkeeper.core.observeFlow
import com.tonapps.tonkeeper.fragment.swap.domain.DexAssetsRepository
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAsset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

class SwapViewModel(
    private val repository: DexAssetsRepository
) : ViewModel() {

    private val _pickedSendAsset = MutableStateFlow<DexAsset?>(null)
    private val _pickedReceiveAsset = MutableStateFlow<DexAsset?>(null)
    private val _events = MutableSharedFlow<SwapEvent>()

    val isLoading = repository.isLoading
    val events: Flow<SwapEvent>
        get() = _events
    val pickedSendAsset: Flow<DexAsset?>
        get() = _pickedSendAsset
    val pickedReceiveAsset: Flow<DexAsset?>
        get() = _pickedReceiveAsset

    init {
        observeFlow(isLoading) { isLoading ->
            if (isLoading) return@observeFlow
            _pickedSendAsset.emit(repository.getDefaultAsset())
        }
    }

    fun onSettingsClicked() {
        Log.wtf("###", "settings clicked")
    }

    fun onCrossClicked() {
        emit(_events, SwapEvent.NavigateBack)
    }

    fun onSendTokenClicked() {
        Log.wtf("###", "send token clicked")
    }

    fun onReceiveTokenClicked() {
        Log.wtf("###", "receive token clicked")
    }

    fun onSwapTokensClicked() {
        val toSend = _pickedSendAsset.value
        _pickedSendAsset.value = _pickedReceiveAsset.value
        _pickedReceiveAsset.value = toSend
    }

    fun onSendAmountChanged(amount: Float) {
    }
}