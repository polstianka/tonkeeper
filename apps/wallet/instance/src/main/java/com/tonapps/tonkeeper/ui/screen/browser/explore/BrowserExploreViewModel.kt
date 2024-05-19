package com.tonapps.tonkeeper.ui.screen.browser.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.ui.screen.browser.explore.list.Item
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.browser.BrowserRepository
import com.tonapps.wallet.data.browser.entities.BrowserDataEntity
import com.tonapps.wallet.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import uikit.extensions.collectFlow

class BrowserExploreViewModel(
    private val walletRepository: WalletRepository,
    private val browserRepository: BrowserRepository,
    private val api: API,
    private val settings: SettingsRepository
): ViewModel() {

    private val _uiItemsFlow = MutableStateFlow<List<Item>>(emptyList())
    val uiItemsFlow = _uiItemsFlow.asStateFlow()

    init {
        combine(settings.countryFlow, walletRepository.activeWalletFlow) { code, wallet ->
            _uiItemsFlow.value = emptyList()
            browserRepository.load(code, wallet.testnet)?.let { setData(it) }
            browserRepository.loadRemote(code, wallet.testnet)?.let { setData(it) }
        }.launchIn(viewModelScope)
    }

    private fun setData(data: BrowserDataEntity) {
        val items = mutableListOf<Item>()
        if (data.apps.isNotEmpty()) {
            items.add(Item.Banners(data.apps, api.config.featuredPlayInterval))
        }
        for (category in data.categories) {
            items.add(Item.Title(category.title))
            for (app in category.apps) {
                items.add(Item.App(app))
            }
        }

        _uiItemsFlow.value = items.toList()
    }
}