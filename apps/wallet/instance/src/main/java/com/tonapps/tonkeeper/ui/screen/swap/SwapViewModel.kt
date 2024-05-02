package com.tonapps.tonkeeper.ui.screen.swap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.token.TokenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SwapViewModel(
    private val walletRepository: WalletRepository,
    private val tokenRepository: TokenRepository,
    private val api: API,
    private val swapRepository: SwapRepository
) : ViewModel() {

    private val sendInputFlow = MutableStateFlow("")
    private val receiveInputFlow = MutableStateFlow("")

    private val _uiModel = MutableStateFlow(SwapUiModel())
    val uiModel: StateFlow<SwapUiModel> = _uiModel

    init {
        viewModelScope.launch(Dispatchers.IO) {
            walletRepository.activeWalletFlow.collect {
                tokenRepository.get(WalletCurrency.TON, it.accountId, it.testnet).firstOrNull()
                    ?.let { token ->
                        swapRepository.setSendToken(
                            AssetModel(
                                token = TokenEntity.TON,
                                balance = token.balance.value,
                                walletAddress = it.address,
                                position = ListCell.Position.SINGLE,
                                fiatBalance = 0f
                            )
                        )
                    }
            }
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

    private fun getBottomButtonState(
        receive: AssetModel?,
        send: AssetModel?,
        sendInput: String,
        receiveInput: String
    ) = if (receive == null || send == null) SwapUiModel.BottomButtonState.Select
    else if (sendInput.isEmpty() || receiveInput.isEmpty()) SwapUiModel.BottomButtonState.Amount
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

