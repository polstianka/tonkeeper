package com.tonapps.tonkeeper.ui.screen.swap

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SwapViewModel(state: SwapViewState) {
    private var _stateFlow = MutableStateFlow(state)
    val stateFlow: StateFlow<SwapViewState> = _stateFlow.asStateFlow()

    fun swapTokens(){
        val temp = _stateFlow.value
        _stateFlow.value = SwapViewState(temp.toTokenTitle, temp.fromTokenTitle, null, null, temp.amount, temp.balance, temp.swapButton)
    }
}