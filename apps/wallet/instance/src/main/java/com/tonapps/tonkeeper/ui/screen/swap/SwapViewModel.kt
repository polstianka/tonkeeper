package com.tonapps.tonkeeper.ui.screen.swap

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.ui.screen.swap.SwapUiModel.Details.DetailUiModel
import com.tonapps.tonkeeper.ui.screen.swap.SwapUiModel.Details.Header
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.entity.SwapDetailsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import com.tonapps.wallet.localization.R as LocalizationR

class SwapViewModel(
    private val swapRepository: SwapRepository,
    private val assetsRepository: AssetsRepository,
) : ViewModel() {

    private val df = DecimalFormat("#.##")

    private val _uiModel = MutableStateFlow(SwapUiModel())
    val uiModel: StateFlow<SwapUiModel> = _uiModel

    init {
        viewModelScope.launch(Dispatchers.IO) {
            assetsRepository.get().firstOrNull()?.let { asset ->
                val sendToken = AssetModel(
                    token = asset.token,
                    balance = asset.value,
                    walletAddress = asset.token.address,
                    position = ListCell.Position.SINGLE,
                    fiatBalance = 0f,
                    isTon = asset.kind == "TON"
                )
                swapRepository.setSendToken(sendToken)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            swapRepository.swapState.collect { state ->
                _uiModel.update {
                    val send = state.send
                    val receive = state.receive
                    val sendInput = state.details?.offerUnits ?: it.sendInput
                    val receiveInput = state.details?.askUnits ?: it.receiveInput
                    val details = state.details?.let { entity ->
                        getUiDetails(send, receive, entity, state)
                    }
                    it.copy(
                        sendToken = send,
                        receiveToken = receive,
                        bottomButtonState = getBottomButtonState(
                            receive = receive,
                            send = send,
                            sendInput = sendInput,
                            receiveInput = receiveInput,
                            loading = details.isNullOrEmpty() && state.isLoading
                        ),
                        sendInput = sendInput,
                        receiveInput = receiveInput,
                        details = details
                    )
                }
            }
        }
    }

    override fun onCleared() {
        swapRepository.clear()
    }

    fun onSendTextChange(s: String) {
        swapRepository.sendTextChanged(s.ifEmpty { "0" })
        if (s.isEmptyOrZero()) {
            resetInput()
            return
        }
        _uiModel.update {
            it.copy(sendInput = s)
        }
    }

    fun onReceiveTextChange(s: String) {
        swapRepository.receiveTextChanged(s)
        _uiModel.update {
            it.copy(receiveInput = s)
        }
    }

    fun swap() {
        swapRepository.swap()
    }

    private fun getUiDetails(
        send: AssetModel?,
        receive: AssetModel?,
        entity: SwapDetailsEntity,
        state: SwapState
    ) = listOf(
        Header(
            swapRate = "1 ${send?.token?.symbol} ≈ ${
                CurrencyFormatter.format(
                    receive?.token?.symbol.orEmpty(),
                    entity.swapRate.toFloat()
                )
            }",
            loading = state.isLoading,
        ),
        DetailUiModel(
            title = LocalizationR.string.price_impact,
            value = df.format(entity.priceImpact.toFloat()) + "%"
        ),
        DetailUiModel(
            title = LocalizationR.string.min_received,
            value = CurrencyFormatter.format(
                receive?.token?.symbol.orEmpty(),
                entity.minReceived.toFloat()
            ).toString()
        ),
        DetailUiModel(
            title = LocalizationR.string.liquidity_provider_fee,
            value = CurrencyFormatter.format(
                receive?.token?.symbol.orEmpty(),
                entity.providerFeeUnits.toFloat()
            ).toString()
        ),
        DetailUiModel(
            title = LocalizationR.string.route,
            value = "${send?.token?.symbol.orEmpty()} » ${receive?.token?.symbol.orEmpty()}"
        ),
        DetailUiModel(
            title = LocalizationR.string.provider,
            value = "STON.fi"
        ),
    )

    private fun resetInput() {
        _uiModel.update {
            it.copy(sendInput = "0", receiveInput = "0")
        }
    }

    private fun getBottomButtonState(
        receive: AssetModel?,
        send: AssetModel?,
        sendInput: String,
        receiveInput: String,
        loading: Boolean
    ) = when {
        loading -> SwapUiModel.BottomButtonState.Loading
        receive == null || send == null -> SwapUiModel.BottomButtonState.Select
        sendInput.isEmpty() || sendInput == "0" -> SwapUiModel.BottomButtonState.Amount
        !sendInput.isEmptyOrZero() && !receiveInput.isEmptyOrZero() -> SwapUiModel.BottomButtonState.Continue
        else -> SwapUiModel.BottomButtonState.Amount
    }

    private fun String.isEmptyOrZero() = isEmpty() || this == "0"
}

data class SwapUiModel(
    val sendToken: AssetModel? = null,
    val receiveToken: AssetModel? = null,
    val bottomButtonState: BottomButtonState = BottomButtonState.Amount,
    val sendInput: String = "0",
    val receiveInput: String = "0",
    val details: List<Details>? = null,
    val loadingDetails: Boolean = false,
) {
    enum class BottomButtonState {
        Select, Amount, Continue, Loading
    }

    sealed class Details {

        data class Header(val swapRate: String, val loading: Boolean) : Details()

        data class DetailUiModel(
            @StringRes val title: Int,
            val value: String
        ) : Details()
    }
}

