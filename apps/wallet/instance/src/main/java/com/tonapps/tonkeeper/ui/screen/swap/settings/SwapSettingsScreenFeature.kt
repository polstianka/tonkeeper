package com.tonapps.tonkeeper.ui.screen.swap.settings

import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.api.swap.SwapRepository
import core.QueueScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uikit.mvi.UiFeature

class SwapSettingsScreenFeature(
    private val swapRepository: SwapRepository
) : UiFeature<SwapSettingsScreenState, SwapSettingsScreenEffect>(SwapSettingsScreenState()) {

    private val queueScope = QueueScope(Dispatchers.IO)

    init {
        viewModelScope.launch {
            loadData()
        }
    }

    private suspend fun loadData() = withContext(Dispatchers.IO) {
        val wallet = App.walletManager.getWalletInfo()!!
        val slip = swapRepository.getSlippage(wallet.address)
        val expert = swapRepository.getExpertMode(wallet.address)

        updateUiState {
            it.copy(
                expertMode = expert,
                slippage = slip,
                asText = slip != 0.01f && slip != 0.03f && slip != 0.05f
            )
        }
    }

    fun setSlippage(slip: Float, asText: Boolean = false) {
        updateUiState {
            it.copy(
                slippage = slip,
                asText = asText,
                error = slip > 1f
            )
        }
    }

    fun setExpertMode(expert: Boolean) {
        updateUiState {
            it.copy(
                expertMode = expert
            )
        }
    }

    fun save() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val wallet = App.walletManager.getWalletInfo()!!
                swapRepository.setSlippage(wallet.address, uiState.value.slippage)
                swapRepository.setExpertMode(wallet.address, uiState.value.expertMode)

                updateUiState {
                    it.copy(
                        saved = true
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        queueScope.cancel()
    }
}