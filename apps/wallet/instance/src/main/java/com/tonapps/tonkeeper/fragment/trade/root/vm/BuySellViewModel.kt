package com.tonapps.tonkeeper.fragment.trade.root.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.core.emit
import com.tonapps.wallet.data.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BuySellViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _currentTab = MutableStateFlow(BuySellTabs.BUY)
    private val _events = MutableSharedFlow<BuySellEvent>()
    val currentTab: StateFlow<BuySellTabs>
        get() = _currentTab
    val events: Flow<BuySellEvent>
        get() = _events
    val country = settingsRepository.countryFlow

    fun onTabSelected(tab: BuySellTabs) {
        if (_currentTab.value == tab) return
        _currentTab.value = tab
        Log.wtf("###", "onTabSelected: $tab")
    }

    fun onCountryLabelClicked() {
        emit(_events, BuySellEvent.NavigateToPickCountry)
    }
}