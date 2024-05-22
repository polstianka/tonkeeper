package com.tonapps.tonkeeper.fragment.stake.balance

import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.core.emit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class StakedBalanceViewModel : ViewModel() {

    private val _events = MutableSharedFlow<StakedBalanceEvent>()

    val events: Flow<StakedBalanceEvent>
        get() = _events
    val args = MutableSharedFlow<StakedBalanceArgs>(replay = 1)

    fun provideArgs(args: StakedBalanceArgs) {
        emit(this.args, args)
    }

    fun onCloseClicked() {
        emit(_events, StakedBalanceEvent.NavigateBack)
    }
}