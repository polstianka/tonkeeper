package com.tonapps.tonkeeper.fragment.swap.info

import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.core.emit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class InfoViewModel : ViewModel() {

    private val _args = MutableSharedFlow<InfoArgs>(replay = 1)
    private val _events = MutableSharedFlow<InfoEvent>()

    val args: Flow<InfoArgs>
        get() = _args
    val events: Flow<InfoEvent>
        get() = _events


    fun provideArgs(infoArgs: InfoArgs) {
        emit(this._args, infoArgs)
    }

    fun onCloseClicked() {
        emit(_events, InfoEvent.Finish)
    }
}