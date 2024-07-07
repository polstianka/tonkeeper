package com.tonapps.tonkeeper.ui.screen.swap

import androidx.lifecycle.ViewModel
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.swap.SwapRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.roundToInt

class SwapSettingsViewModel(
    private val swapRepository: SwapRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SwapSettingsUiState())
    val uiState: StateFlow<SwapSettingsUiState> = _uiState

    init {
        val slippageTolerance = settingsRepository.slippage
        val expert = settingsRepository.expertMode
        _uiState.update {
            it.copy(
                tolerancePercent = slippageTolerance.roundToInt(),
                expert = expert
            )
        }
    }

    fun onSuggestClicked(percent: Int) {
        _uiState.update {
            it.copy(tolerancePercent = percent)
        }
    }

    fun percentChanged(s: String) {
        if (s.isNotEmpty()) {
            val value = s.toInt()
            _uiState.update { it.copy(tolerancePercent = value) }
        }
    }

    fun onSwitchChanged(on: Boolean) {
        _uiState.update { it.copy(expert = on) }
    }

    fun onSaveClick() {
        val state = uiState.value
        swapRepository.setSlippageTolerance(state.tolerancePercent / 100f)
        settingsRepository.slippage = state.tolerancePercent.toFloat()
        settingsRepository.expertMode = state.expert
    }
}

data class SwapSettingsUiState(
    val tolerancePercent: Int = 1,
    val expert: Boolean = false,
    val suggestedToleranceList: List<Int> = listOf(1, 3, 5)
)