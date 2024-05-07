package com.tonapps.tonkeeper.fragment.trade.root.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class BuySellViewModel : ViewModel() {

    private val currentTab = MutableStateFlow(BuySellTabs.BUY)

    fun onTabSelected(tab: BuySellTabs) {
        if (currentTab.value == tab) return
        currentTab.value = tab
        Log.wtf("###", "onTabSelected: $tab")
    }

}