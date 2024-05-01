package com.tonapps.tonkeeper.ui.screen.swap

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SwapRepository {
    private var _sendToken = MutableStateFlow<AssetModel?>(null)
    val sendToken: StateFlow<AssetModel?> = _sendToken

    private var _receiveToken = MutableStateFlow<AssetModel?>(null)
    val receiveToken: StateFlow<AssetModel?> = _receiveToken

    fun setSendToken(model: AssetModel) {
        _sendToken.value = model
    }

    fun setReceiveToken(model: AssetModel) {
        _receiveToken.value = model
    }

}