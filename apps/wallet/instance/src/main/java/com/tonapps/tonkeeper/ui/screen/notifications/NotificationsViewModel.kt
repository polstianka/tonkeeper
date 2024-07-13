package com.tonapps.tonkeeper.ui.screen.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.ui.screen.notifications.list.Item
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.tonconnect.TonConnectRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.take

class NotificationsViewModel(
    private val walletRepository: WalletRepository,
    private val tonConnectRepository: TonConnectRepository,
    private val settingsRepository: SettingsRepository,
): ViewModel() {

    private val _uiItemsFlow = MutableStateFlow<List<Item>?>(null)
    val uiItemsFlow = _uiItemsFlow.asStateFlow().filterNotNull()

    init {
        combine(
            walletRepository.activeWalletFlow,
            tonConnectRepository.appsFlow.take(1)
        ) { wallet, apps ->
            val myApps = apps.filter { it.walletId == wallet.id }
            val uiItems = mutableListOf<Item>()
            uiItems.add(Item.Wallet(
                pushEnabled = settingsRepository.getPushWallet(wallet.id),
                walletId = wallet.id
            ))
            uiItems.add(Item.Space)
            if (myApps.isNotEmpty()) {
                uiItems.add(Item.AppsHeader)
                uiItems.add(Item.Space)
                for ((index, app) in myApps.withIndex()) {
                    val position = ListCell.getPosition(myApps.size, index)
                    uiItems.add(Item.App(app, position))
                }
                uiItems.add(Item.Space)
            }
            _uiItemsFlow.value = uiItems
        }.flowOn(Dispatchers.IO).launchIn(viewModelScope)
    }
}