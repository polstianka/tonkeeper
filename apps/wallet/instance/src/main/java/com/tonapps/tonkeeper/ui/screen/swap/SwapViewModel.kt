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
                swapRepository.swapData
            ) { send, receive, data ->
                _uiModel.update {
                    val sendInput = data?.offerUnits ?: it.sendInput
                    val receiveInput = data?.askUnits ?: it.receiveInput
                    it.copy(
                        sendToken = send,
                        receiveToken = receive,
                        bottomButtonState = getBottomButtonState(
                            receive,
                            send,
                            sendInput,
                            receiveInput
                        ),
                        sendInput = sendInput,
                        receiveInput = receiveInput
                    )
                }
            }.flowOn(Dispatchers.IO).launchIn(viewModelScope)
        }
    }

    fun onSendTextChange(s: String) {
        swapRepository.sendTextChanged(s)
        if (s == "0") {
            resetInput()
            return
        }
        _uiModel.update {
            it.copy(sendInput = s)
        }
    }

    fun onReceiveTextChange(s: String) {
        swapRepository.receiveTextChanged(s)
        if (s == "0") {
            resetInput()
            return
        }
        _uiModel.update {
            it.copy(receiveInput = s)
        }
    }

    fun swap() {
        swapRepository.swap()
    }

    private fun resetInput() {
        _uiModel.update {
            it.copy(sendInput = "0", receiveInput = "0")
        }
    }

    private fun getBottomButtonState(
        receive: AssetModel?,
        send: AssetModel?,
        sendInput: String,
        receiveInput: String
    ) = if (receive == null || send == null) SwapUiModel.BottomButtonState.Select
    else if (sendInput.isEmpty() || sendInput == "0" || receiveInput.isEmpty() || receiveInput == "0") SwapUiModel.BottomButtonState.Amount
    else SwapUiModel.BottomButtonState.Continue
}

data class SwapUiModel(
    val sendToken: AssetModel? = null,
    val receiveToken: AssetModel? = null,
    val bottomButtonState: BottomButtonState = BottomButtonState.Amount,
    val sendInput: String = "0",
    val receiveInput: String = "0"
) {
    enum class BottomButtonState {
        Select, Amount, Continue
    }
}

