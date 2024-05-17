package com.tonapps.tonkeeper.fragment.swap.pick_asset

import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.core.emit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class PickAssetViewModel : ViewModel() {

    private val args = MutableSharedFlow<PickAssetArgs>(replay = 1)
    private val _events = MutableSharedFlow<PickAssetEvent>()

    val events: Flow<PickAssetEvent>
        get() = _events
    fun provideArgs(args: PickAssetArgs) {
        emit(this.args, args)
    }

    fun onCloseClicked() {
        emit(_events, PickAssetEvent.NavigateBack)
    }


}