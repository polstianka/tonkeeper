package com.tonapps.tonkeeper.fragment.trade.pick_currency

import android.util.Log
import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.core.emit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class PickCurrencyViewModel : ViewModel() {

    private val _events = MutableSharedFlow<PickCurrencyEvent>()
    val events: Flow<PickCurrencyEvent>
        get() = _events
    fun onCrossClicked() {
        emit(_events, PickCurrencyEvent.NavigateBack)
    }

    fun provideArgs(pickCurrencyFragmentArgs: PickCurrencyFragmentArgs) {
        Log.wtf("###", "pickCurrencyArgs: $pickCurrencyFragmentArgs")
    }
}