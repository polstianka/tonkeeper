package com.tonapps.tonkeeper.fragment.trade.pick_operator

import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.core.emit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

class PickOperatorViewModel : ViewModel() {

    private val _events = MutableSharedFlow<PickOperatorEvents>()
    val events: Flow<PickOperatorEvents>
        get() = _events
    private val _subtitleText = MutableStateFlow("")

    val subtitleText: Flow<String>
        get() = _subtitleText

    fun provideArguments(arguments: PickOperatorFragmentArgs) {
        _subtitleText.value = arguments.name
    }

    fun onChevronClicked() {
        emit(_events, PickOperatorEvents.NavigateBack)
    }

    fun onCrossClicked() {
        emit(_events, PickOperatorEvents.CloseFlow)
    }
}