package com.tonapps.tonkeeper.fragment.swap.root

import android.util.Log
import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.core.emit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class SwapViewModel : ViewModel() {

    private val _events = MutableSharedFlow<SwapEvent>()

    val events: Flow<SwapEvent>
        get() = _events
    fun onSettingsClicked() {
        Log.wtf("###", "settings clicked")
    }

    fun onCrossClicked() {
        emit(_events, SwapEvent.NavigateBack)
    }

}