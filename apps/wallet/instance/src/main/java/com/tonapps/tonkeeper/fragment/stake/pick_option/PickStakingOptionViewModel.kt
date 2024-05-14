package com.tonapps.tonkeeper.fragment.stake.pick_option

import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.core.emit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class PickStakingOptionViewModel : ViewModel() {

    private val _events = MutableSharedFlow<PickStakingOptionEvent>()
    val events: Flow<PickStakingOptionEvent>
        get() = _events
    fun onChevronClicked() {
        emit(_events, PickStakingOptionEvent.NavigateBack)
    }

    fun onCrossClicked() {
        emit(_events, PickStakingOptionEvent.CloseFlow)
    }
}