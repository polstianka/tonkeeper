package com.tonapps.tonkeeper.fragment.trade.pick_currency

import android.util.Log
import androidx.lifecycle.ViewModel
import com.tonapps.uikit.list.BaseListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

class PickCurrencyViewModel : ViewModel() {

    private val _events = MutableSharedFlow<PickCurrencyEvent>()
    val events: Flow<PickCurrencyEvent>
        get() = _events
    private val _items = MutableStateFlow(emptyList<BaseListItem>())
    val items: Flow<List<BaseListItem>>
        get() = _items

    fun provideArgs(pickCurrencyFragmentArgs: PickCurrencyFragmentArgs) {
        Log.wtf("###", "pickCurrencyArgs: $pickCurrencyFragmentArgs")
    }

    fun onCurrencyClicked(code: String) {
        Log.wtf("###", "onCurrencyClicked: $code")
    }
}