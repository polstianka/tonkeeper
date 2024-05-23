package com.tonapps.tonkeeper.fragment.stake.unstake

import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.core.emit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class UnstakeViewModel : ViewModel() {
    private val args = MutableSharedFlow<UnstakeArgs>(replay = 1)
    private val _events = MutableSharedFlow<UnstakeEvent>()

    val events: Flow<UnstakeEvent>
        get() = _events
    fun provideArgs(args: UnstakeArgs) {
        emit(this.args, args)
    }

    fun onCloseClicked() {
        emit(_events, UnstakeEvent.NavigateBack)
    }
}