package com.tonapps.tonkeeper.ui.screen.purchase

import android.app.Application
import com.tonapps.tonkeeper.extensions.getNormalizeCountryFlow
import com.tonapps.tonkeeper.ui.base.BaseWalletVM
import com.tonapps.tonkeeper.ui.screen.purchase.list.Item
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.purchase.PurchaseRepository
import com.tonapps.wallet.data.purchase.entity.PurchaseMethodEntity
import com.tonapps.wallet.data.settings.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class PurchaseViewModel(
    app: Application,
    private val wallet: WalletEntity,
    private val settingsRepository: SettingsRepository,
    private val purchaseRepository: PurchaseRepository,
    private val api: API,
): BaseWalletVM(app) {

    enum class Tab {
        BUY, SELL
    }

    val countryFlow = settingsRepository.getNormalizeCountryFlow(api)

    private val _tabFlow = MutableStateFlow(Tab.BUY)
    val tabFlow = _tabFlow.asStateFlow()

    val country: String
        get() = (countryFlow as? StateFlow)?.value ?: settingsRepository.country

    val tabName: String
        get() = when (tabFlow.value) {
            Tab.BUY -> "buy"
            Tab.SELL -> "sell"
        }

    private val dataFlow = countryFlow.map { country ->
        purchaseRepository.get(wallet.testnet, country, settingsRepository.getLocale())
    }.filterNotNull().flowOn(Dispatchers.IO)

    val uiItemsFlow = combine(dataFlow, tabFlow) { (buy, sell), tab ->
        val categories = if (tab == Tab.BUY) buy else sell
        val items = mutableListOf<Item>()
        for ((categoryIndex, category) in categories.withIndex()) {
            if (categoryIndex != 0) {
                items.add(Item.Space)
            }
            items.add(Item.Title(category.title))
            for ((methodIndex, method) in category.items.withIndex()) {
                val position = ListCell.getPosition(category.items.size, methodIndex)
                items.add(Item.Method(method, position, category.type))
            }
        }
        items.add(Item.Space)
        items
    }

    fun isPurchaseOpenConfirm(method: PurchaseMethodEntity) = settingsRepository.isPurchaseOpenConfirm(wallet.id, method.id)

    fun disableConfirmDialog(
        wallet: WalletEntity,
        method: PurchaseMethodEntity
    ) {
        settingsRepository.disablePurchaseOpenConfirm(wallet.id, method.id)
    }

    fun setTab(tab: Tab) {
        _tabFlow.value = tab
    }

}