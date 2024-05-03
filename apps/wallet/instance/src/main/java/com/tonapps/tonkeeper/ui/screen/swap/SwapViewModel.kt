package com.tonapps.tonkeeper.ui.screen.swap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SwapViewModel(
    private val swapRepository: SwapRepository
) : ViewModel() {

    private val sendInputFlow = MutableStateFlow("")
    private val receiveInputFlow = MutableStateFlow("")

    private val _uiModel = MutableStateFlow(SwapUiModel())
    val uiModel: StateFlow<SwapUiModel> = _uiModel

    init {
        viewModelScope.launch(Dispatchers.IO) {
            swapRepository.init()
        }

        viewModelScope.launch {
            combine(
                swapRepository.sendToken,
                swapRepository.receiveToken,
                sendInputFlow,
                receiveInputFlow,
            ) { send, receive, sendInput, receiveInput ->
                _uiModel.update {
                    it.copy(
                        sendToken = send,
                        receiveToken = receive,
                        bottomButtonState = getBottomButtonState(
                            receive,
                            send,
                            sendInput,
                            receiveInput
                        )
                    )
                }
            }.flowOn(Dispatchers.IO).launchIn(viewModelScope)
        }
    }

    fun onSendTextChange(s: String) {
        sendInputFlow.value = s
    }

    fun onReceiveTextChange(s: String) {
        receiveInputFlow.value = s
    }

    fun swap() {
        swapRepository.swap()
    }

    private fun getBottomButtonState(
        receive: AssetModel?,
        send: AssetModel?,
        sendInput: String,
        receiveInput: String
    ) = if (receive == null || send == null) SwapUiModel.BottomButtonState.Select
    else if (sendInput.isEmpty() && sendInput != "0" || receiveInput.isEmpty()) SwapUiModel.BottomButtonState.Amount
    else SwapUiModel.BottomButtonState.Continue
}

data class SwapUiModel(
    val sendToken: AssetModel? = null,
    val receiveToken: AssetModel? = null,
    val bottomButtonState: BottomButtonState = BottomButtonState.Amount
) {
    enum class BottomButtonState {
        Select, Amount, Continue
    }
}

