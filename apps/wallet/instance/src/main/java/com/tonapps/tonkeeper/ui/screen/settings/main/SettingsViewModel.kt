package com.tonapps.tonkeeper.ui.screen.settings.main

import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.extensions.capitalized
import com.tonapps.tonkeeper.ui.screen.settings.main.list.Item
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.account.WalletType
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.core.SearchEngine
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.localization.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import uikit.extensions.collectFlow

class SettingsViewModel(
    private val walletRepository: WalletRepository,
    private val settings: SettingsRepository,
    private val api: API
): ViewModel() {

    private val _uiItemsFlow = MutableStateFlow<List<Item>>(emptyList())
    val uiItemsFlow = _uiItemsFlow.asStateFlow().filter { it.isNotEmpty() }

    init {
        combine(
            walletRepository.activeWalletFlow,
            settings.currencyFlow,
            settings.languageFlow,
            settings.searchEngineFlow
        ) { wallet, currency, language, searchEngine ->
            buildUiItems(wallet, currency, language, searchEngine)
        }.launchIn(viewModelScope)
    }

    fun setSearchEngine(searchEngine: SearchEngine?) {
        settings.searchEngine = searchEngine ?: SearchEngine.GOOGLE
    }

    fun signOut() {
        walletRepository.removeCurrent()
    }

    private fun buildUiItems(
        wallet: WalletEntity,
        currency: WalletCurrency,
        language: Language,
        searchEngine: SearchEngine
    ) {
        val uiItems = mutableListOf<Item>()
        uiItems.add(Item.Account(wallet))

        uiItems.add(Item.Space)
        uiItems.add(Item.Theme(ListCell.Position.FIRST))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            uiItems.add(Item.Widget(ListCell.Position.MIDDLE))
        }
        uiItems.add(Item.Notifications(ListCell.Position.MIDDLE))
        uiItems.add(Item.Currency(currency.code, ListCell.Position.LAST))

        uiItems.add(Item.Space)
        uiItems.add(Item.Security(ListCell.Position.FIRST))
        uiItems.add(Item.SearchEngine(searchEngine, ListCell.Position.MIDDLE))
        uiItems.add(Item.Language(language.nameLocalized.capitalized, ListCell.Position.LAST))

        uiItems.add(Item.Space)
        uiItems.add(Item.FAQ(ListCell.Position.FIRST, api.config.faqUrl))
        uiItems.add(Item.Support(ListCell.Position.MIDDLE, api.config.directSupportUrl))
        uiItems.add(Item.News(ListCell.Position.MIDDLE, api.config.tonkeeperNewsUrl))
        uiItems.add(Item.Contact(ListCell.Position.MIDDLE, api.config.supportLink))
        uiItems.add(Item.Rate(ListCell.Position.MIDDLE))
        uiItems.add(Item.Legal(ListCell.Position.LAST))

        uiItems.add(Item.Space)
        if (wallet.type == WalletType.Watch) {
            uiItems.add(Item.DeleteWatchAccount(ListCell.Position.SINGLE))
        } else {
            uiItems.add(Item.Logout(ListCell.Position.SINGLE))
        }
        uiItems.add(Item.Space)
        uiItems.add(Item.Logo)

        _uiItemsFlow.value = uiItems
    }
}