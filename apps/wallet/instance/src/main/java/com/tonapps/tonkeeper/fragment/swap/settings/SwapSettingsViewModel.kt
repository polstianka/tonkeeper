package com.tonapps.tonkeeper.fragment.swap.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.core.emit
import com.tonapps.tonkeeper.fragment.swap.domain.model.SwapSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SwapSettingsViewModel : ViewModel() {

    private val _events = MutableSharedFlow<SwapSettingsEvent>(replay = 1)
    private val _screenState = MutableStateFlow<SwapSettings?>(null)

    val events: Flow<SwapSettingsEvent>
        get() = _events
    val screenState = _screenState.filterNotNull()

    fun provideArgs(args: SwapSettingsArgs) {
        _screenState.value = args.settings
        if (args.settings is SwapSettings.ExpertMode) {
            emit(_events, SwapSettingsEvent.FillInput(args.settings.slippagePercent.toString()))
        }
    }

    fun onCloseClick() {
        emit(_events, SwapSettingsEvent.NavigateBack)
    }

    private var lastPercentage = 1
    fun onInputChanged(text: String) {
        lastPercentage = text.toIntOrNull() ?: return
        if (lastPercentage > 100) {
            lastPercentage = 100
            emit(_events, SwapSettingsEvent.FillInput("100"))
        }
        updateState(SwapSettings.ExpertMode(lastPercentage))
    }

    fun onSlippageOptionOneChecked() {
        updateState(SwapSettings.NoviceMode.One)
    }

    fun onSlippageOptionTwoChecked() {
        updateState(SwapSettings.NoviceMode.Three)
    }

    fun onSlippageOptionThreeChecked() {
        updateState(SwapSettings.NoviceMode.Five)
    }

    private var lastSlippage: SwapSettings.NoviceMode = SwapSettings.NoviceMode.One
    fun onExpertModeChecked() = viewModelScope.launch {
        val state = screenState.first()
        val newState = when (state) {
            is SwapSettings.ExpertMode -> lastSlippage
            is SwapSettings.NoviceMode -> SwapSettings.ExpertMode(lastPercentage)
        }
        updateState(newState)
    }

    private fun updateState(newState: SwapSettings) = viewModelScope.launch {
        val currentState = screenState.first()
        when {
            newState.isExpertModeOn == currentState.isExpertModeOn -> Unit
            newState.isExpertModeOn -> {
                _events.emit(SwapSettingsEvent.FillInput(lastPercentage.toString()))
                lastSlippage = currentState as SwapSettings.NoviceMode
            }

            else -> _events.emit(SwapSettingsEvent.FillInput(""))
        }
        _screenState.value = newState
    }

    fun onButtonClicked() = viewModelScope.launch {
        val state = screenState.first()
        val event = SwapSettingsEvent.ReturnResult(state)
        _events.emit(event)
    }
}