package com.tonapps.tonkeeper.fragment.stake.pool_details

import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.core.emit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class PoolDetailsViewModel : ViewModel() {

    private val _events = MutableSharedFlow<PoolDetailsEvent>(replay = 1)
    private val args = MutableSharedFlow<PoolDetailsFragmentArgs>()

    val events: Flow<PoolDetailsEvent>
        get() = _events
    fun provideArgs(args: PoolDetailsFragmentArgs) {
        emit(this.args, args)
    }

    fun onCloseClicked() {
        emit(_events, PoolDetailsEvent.FinishFlow)
    }

    fun onChevronClicked() {
        emit(_events, PoolDetailsEvent.NavigateBack)
    }
}