package com.tonapps.tonkeeper.ui.screen.swap

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SwapSettingsViewModel(
    private val swapRepository: com.tonapps.wallet.data.swap.SwapRepository
) : ViewModel() {

    private val _suggestedTolerance = MutableStateFlow(emptyList<Int>())
    val suggestedTolerance: StateFlow<List<Int>> = _suggestedTolerance

    private val _selectedSuggestion = MutableStateFlow<Int?>(null)
    val selectedSuggestion: StateFlow<Int?> = _selectedSuggestion

    init {
        _suggestedTolerance.value = suggestedToleranceList
    }

    fun onSuggestClicked(percent: Int) {
        _selectedSuggestion.value = percent
    }

    fun percentChanged(s: String) {
        if (s.isNotEmpty()) {
            val value = s.toInt() / 100f
            swapRepository.setSlippageTolerance(value)
        }
    }

    companion object {
        private val suggestedToleranceList = listOf(1, 3, 5)
    }
}