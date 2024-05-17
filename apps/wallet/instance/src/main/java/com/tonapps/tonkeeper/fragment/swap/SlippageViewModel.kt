package com.tonapps.tonkeeper.fragment.swap

import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.fragment.swap.model.Slippage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull

class SlippageViewModel(
): ViewModel() {

    private val _selectedFlow = MutableStateFlow<Slippage?>(null)
    val selectedFlow = _selectedFlow.asStateFlow().filterNotNull()

    private val _isExpertFlow = MutableStateFlow(false)
    val isExpertFlow = _isExpertFlow.asStateFlow()

    fun onSelect(slippage: Slippage) {
        _selectedFlow.value = slippage
    }

}