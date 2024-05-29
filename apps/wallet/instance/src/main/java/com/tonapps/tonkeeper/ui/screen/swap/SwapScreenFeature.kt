package com.tonapps.tonkeeper.ui.screen.swap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tonapps.tonkeeper.api.swap.StonfiSwapAsset
import com.tonapps.tonkeeper.api.swap.SwapSimulateData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import uikit.mvi.UiFeature

class SwapScreenFeature : UiFeature<SwapScreenState, SwapScreenEffect>(SwapScreenState()) {

    private val _onReadyView: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val onReadyView = _onReadyView.asStateFlow()

    private val _swap = MutableLiveData(SwapData())
    val swap: LiveData<SwapData> = _swap

    fun setInitialTo(toToken: String?) {
        _swap.value?.initialTo = toToken
    }

    fun setAmount(amount: String) {
        _swap.value = _swap.value?.copy(amountRaw = amount)
    }

    fun setAmountRec(amount: String) {
        _swap.value = _swap.value?.copy(amountToRaw = amount)
    }

    fun setCurrentFrom(from: Boolean) {
        _swap.value?.currentFrom = from
    }

    fun setFlipping(f: Boolean) {
        _swap.value?.flip = f
    }

    fun flip() {
        val from = swap.value?.from
        val to = swap.value?.to

        val aFrom = swap.value?.amountRaw
        val aTo = swap.value?.amountToRaw
        _swap.value = _swap.value?.copy(
            from = to,
            to = from,
            amountRaw = aTo ?: "0",
            amountToRaw = aFrom ?: "0",
            flip = true
        )
    }

    fun setFrom(fromAsset: StonfiSwapAsset) {
        _swap.value = _swap.value?.copy(from = fromAsset)
    }

    fun setTo(toAsset: StonfiSwapAsset) {
        _swap.value?.initialTo = null
        _swap.value = _swap.value?.copy(to = toAsset)
    }

    fun clearFrom() {
        _swap.value = _swap.value?.copy(from = null)
    }

    fun clearTo() {
        _swap.value = _swap.value?.copy(to = null)
    }

    fun setSimulation(simulateData: SwapSimulateData) {
        _swap.value = _swap.value?.copy(simulateData = simulateData)
    }

    fun setSettings(slippage: Float, expertMode: Boolean) {
        _swap.value = _swap.value?.copy(slippage = slippage, expertMode = expertMode)
    }

    fun readyView() {
        _onReadyView.value = true
    }

    fun nextPage() {
        updateUiState { it.copy(currentPage = it.currentPage + 1) }
    }

    fun prevPage() {
        updateUiState { it.copy(currentPage = it.currentPage - 1) }
    }

    fun setCurrentPage(index: Int) {
        updateUiState { it.copy(currentPage = index) }
    }
}