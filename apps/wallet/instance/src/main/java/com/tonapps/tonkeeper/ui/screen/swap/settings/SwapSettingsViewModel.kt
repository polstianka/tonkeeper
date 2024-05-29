package com.tonapps.tonkeeper.ui.screen.swap.settings

import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.ui.screen.swap.data.SlippageTolerance
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SwapSettingsViewModel(
    currentSettings: SwapSettings
): ViewModel() {
    private val _uiState = MutableStateFlow(currentSettings)
    val uiState: StateFlow<SwapSettings> = _uiState.asStateFlow()

    fun setEnableExpertMode(enabled: Boolean) =
        _uiState.tryEmit(uiState.value.copy(
            enableExpertMode = enabled
        ))

    fun setSlippageTolerance(slippageTolerance: SlippageTolerance) =
        _uiState.tryEmit(uiState.value.copy(
            slippageTolerance = slippageTolerance
        ))
}