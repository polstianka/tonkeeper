package com.tonapps.tonkeeper.fragment.swap.settings

import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.core.emit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class SwapSettingsViewModel : ViewModel() {

    private val args = MutableSharedFlow<SwapSettingsArgs>(replay = 1)
    private val _events = MutableSharedFlow<SwapSettingsEvent>()

    val events: Flow<SwapSettingsEvent>
        get() = _events
    fun provideArgs(args: SwapSettingsArgs) {
        emit(this.args, args)
    }

    fun onCloseClick() {
        emit(_events, SwapSettingsEvent.NavigateBack)
    }
}