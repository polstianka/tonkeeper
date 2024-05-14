package com.tonapps.tonkeeper.fragment.stake.pick_pool

import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.core.emit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map

class PickPoolViewModel : ViewModel() {

    private val args = MutableSharedFlow<PickPoolFragmentArgs>(replay = 1)
    private val _events = MutableSharedFlow<PickPoolEvents>()
    val events: Flow<PickPoolEvents>
        get() = _events
    val title = args.map { it.title }
    fun provideArguments(pickPoolFragmentArgs: PickPoolFragmentArgs) {
        emit(args, pickPoolFragmentArgs)
    }

    fun onChevronClicked() {
        emit(_events, PickPoolEvents.NavigateBack)
    }

    fun onCloseClicked() {
        emit(_events, PickPoolEvents.CloseFlow)
    }
}