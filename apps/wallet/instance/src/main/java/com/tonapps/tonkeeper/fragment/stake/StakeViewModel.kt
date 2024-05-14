package com.tonapps.tonkeeper.fragment.stake

import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.core.emit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class StakeViewModel : ViewModel() {

    private val _events = MutableSharedFlow<StakeEvent>()
    val events: Flow<StakeEvent>
        get() = _events
    fun onCloseClicked() {
        emit(_events, StakeEvent.NavigateBack)
    }

    fun onInfoClicked() {
        emit(_events, StakeEvent.ShowInfo)
    }

}