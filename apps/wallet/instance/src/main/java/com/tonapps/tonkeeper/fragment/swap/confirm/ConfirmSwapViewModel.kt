package com.tonapps.tonkeeper.fragment.swap.confirm

import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.core.emit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class ConfirmSwapViewModel : ViewModel() {
    private val args = MutableSharedFlow<ConfirmSwapArgs>(replay = 1)
    private val _events = MutableSharedFlow<ConfirmSwapEvent>()

    val events: Flow<ConfirmSwapEvent>
        get() = _events

    fun provideArgs(args: ConfirmSwapArgs) {
        emit(this.args, args)
    }

    fun onCloseClicked() {
        emit(_events, ConfirmSwapEvent.CloseFlow)
    }


}