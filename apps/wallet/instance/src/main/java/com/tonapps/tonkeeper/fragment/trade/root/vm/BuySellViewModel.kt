package com.tonapps.tonkeeper.fragment.trade.root.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BuySellViewModel : ViewModel() {

    private val _currentTab = MutableStateFlow(BuySellTabs.BUY)
    val currentTab: StateFlow<BuySellTabs>
        get() = _currentTab

    fun onTabSelected(tab: BuySellTabs) {
        if (_currentTab.value == tab) return
        _currentTab.value = tab
        Log.wtf("###", "onTabSelected: $tab")
    }

}