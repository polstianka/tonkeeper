package com.tonapps.tonkeeper.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

inline fun <T> ViewModel.observeFlow(
    flow: Flow<T>,
    crossinline action: (T) -> Unit
) {
    viewModelScope.launch {
        flow.collectLatest { action(it) }
    }
}
