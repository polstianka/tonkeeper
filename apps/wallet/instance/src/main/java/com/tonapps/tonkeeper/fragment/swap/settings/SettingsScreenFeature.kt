package com.tonapps.tonkeeper.fragment.swap.settings

import androidx.lifecycle.ViewModel
import com.tonapps.blockchain.Coin
import com.tonapps.wallet.data.settings.SettingsRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsScreenFeature(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsScreenState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<SettingsScreenEffect>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val uiEffect: SharedFlow<SettingsScreenEffect> = _uiEffect.asSharedFlow()

    fun load() {
        val slippage = settingsRepository.slippage
        _uiState.update { oldState ->
            oldState.copy(
                slippage = slippage,
                expertMode = settingsRepository.expertMode
            )
        }
        _uiEffect.tryEmit(SettingsScreenEffect.UpdateSlippage(Coin.prepareValue(slippage.toString())))
    }

    fun onSlippageChanged(value: String) {
        val slippage = value.toFloatOrNull() ?: 0F
        _uiState.update { oldState ->
            oldState.copy(slippage = slippage)
        }
    }

    fun onSlippageSuggestionClick(value: String) {
        val suggestionValue = value.filter { it.isDigit() }.toFloatOrNull() ?: 0F
        _uiState.update { oldState ->
            oldState.copy(slippage = suggestionValue)
        }
        _uiEffect.tryEmit(SettingsScreenEffect.UpdateSlippage(suggestionValue.toString()))
    }

    fun expertModeCheckedChanged(checked: Boolean) {
        _uiState.update { oldState ->
            oldState.copy(expertMode = checked)
        }
    }

    fun onSaveClick() {
        val state = _uiState.value
        settingsRepository.slippage = state.slippage
        settingsRepository.expertMode = state.expertMode
        _uiEffect.tryEmit(SettingsScreenEffect.Finish)
    }
}