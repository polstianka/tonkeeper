package com.tonapps.tonkeeper.fragment.stake.confirm

import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.core.emit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

class ConfirmStakeViewModel : ViewModel() {
    private val args = MutableSharedFlow<ConfirmStakeArgs>(replay = 1)
    private val _events = MutableSharedFlow<ConfirmStakeEvent>()

    val events: Flow<ConfirmStakeEvent>
        get() = _events

    fun provideArgs(args: ConfirmStakeArgs) {
        emit(this.args, args)
    }

    fun onChevronClicked() {
        emit(_events, ConfirmStakeEvent.NavigateBack)
    }

    fun onCrossClicked() {
        emit(_events, ConfirmStakeEvent.CloseFlow)
    }
}