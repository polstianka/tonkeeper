package com.tonapps.tonkeeper.fragment.fiat

import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.core.fiat.models.FiatData
import com.tonapps.tonkeeper.core.fiat.models.FiatItem
import com.tonapps.tonkeeper.ui.adapter.Item
import com.tonapps.tonkeeper.ui.screen.settings.currency.CurrencyViewModel
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.settings.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class FiatScreenViewModel(
    walletRepository: WalletRepository,
    val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _fiatMethodsFlow = makeMethodsFlow(settingsRepository.languageFlow.map { it.code })
    private val _selectedMethodFlow = MutableStateFlow(Selected(null, null))

    data class Selected(
        val buy: String?,
        val sell: String?
    )

    data class Currency(
        val code: String,
        @StringRes val name: Int
    )

    enum class Action {
        Buy, Sell
    }

    private val _actionFlow = MutableStateFlow(Action.Buy)
    val selectedActionFlow = _actionFlow.asStateFlow()

    fun setAction(action: Action) {
        _actionFlow.value = action
    }

    fun setMethod(method: String) {
        when (_actionFlow.value) {
            Action.Buy -> _selectedMethodFlow.value = _selectedMethodFlow.value.copy(buy = method)
            Action.Sell -> _selectedMethodFlow.value = _selectedMethodFlow.value.copy(sell = method)
        }
    }

    fun setCurrency(currency: String) {
        _selectedCurrencyFlow.value = currency
    }

    data class Method (
        val method: FiatItem,
        val action: Action,
        val currency: Currency,
        val wallet: WalletEntity
    ) {
        suspend fun getUrl(): String {
            return when (action) {
                Action.Buy -> App.fiat.replaceUrl(method.actionButton.url, wallet.address, currencyFrom = currency.code, currencyTo = "TON")
                Action.Sell -> App.fiat.replaceUrl(method.actionButton.url, wallet.address, currencyFrom = "TON", currencyTo = currency.code)
            }
        }
    }

    private val _selectedCurrencyFlow =
        MutableStateFlow(if (settingsRepository.currency.fiat) settingsRepository.currency.code else WalletCurrency.FIAT.first())
    val selectedCurrencyFlow =
        _selectedCurrencyFlow.map { Currency(it, CurrencyViewModel.getNameResIdForCurrency(it)) }
    val currencyUiItemsFlow = _selectedCurrencyFlow.map(this::buildCurrencyListItems)
    val selectedMethodFlow =
        combine(walletRepository.activeWalletFlow, _fiatMethodsFlow, _actionFlow, _selectedMethodFlow, selectedCurrencyFlow, this::getSelectedMethod)
    val uiItemsFlow =
        combine(_fiatMethodsFlow, _actionFlow, selectedMethodFlow, this::buildMethodItems)


    private fun buildCurrencyListItems(selectedCode: String): List<BaseListItem> {
        val currencies = WalletCurrency.FIAT
        val items = mutableListOf<BaseListItem>()
        for ((index, currency) in currencies.withIndex()) {
            val item = com.tonapps.tonkeeper.ui.screen.settings.currency.list.Item(
                currency = currency,
                nameResId = CurrencyViewModel.getNameResIdForCurrency(currency),
                selected = currency == selectedCode,
                position = ListCell.getPosition(currencies.size, index)
            )
            items.add(item)
        }
        return items
    }

    private fun buildMethodItems(
        data: FiatData,
        action: Action,
        selected: Method
    ): List<BaseListItem> {
        val ids = mutableSetOf<String>()
        val uiItems = mutableListOf<BaseListItem>()

        val categories = when (action) {
            Action.Buy -> data.buy
            Action.Sell -> data.sell
        }

        for (category in categories) {
            val list = category.items.filter { !ids.contains(it.title) }
            if (list.isEmpty()) {
                continue
            }

            if (uiItems.isNotEmpty()) {
                uiItems.add(Item.Offset8)
            }

            uiItems.add(Item.TitleH3(category.title))
            for ((index, item) in list.withIndex()) {
                val position = ListCell.getPosition(list.size, index)
                // ids.add(item.title)
                uiItems.add(Item.FiatMethod(
                    position = position,
                    id = item.id,
                    title = item.title,
                    subtitle = item.subtitle,
                    iconUri = Uri.parse(item.iconUrl),
                    checked = item.id == selected.method.id,
                    onClickListener = { setMethod(item.id) }
                ))
            }
        }

        return uiItems
    }

    private fun getSelectedMethod(wallet: WalletEntity, data: FiatData, action: Action, selected: Selected, currency: Currency): Method {
        val item = getSelectedMethod(data, action, selected)
        return Method(method = item, wallet = wallet, action = action, currency = currency)
    }

    private fun getSelectedMethod(data: FiatData, action: Action, selected: Selected): FiatItem {
        val ids = mutableSetOf<String>()

        val categories = when (action) {
            Action.Buy -> data.buy
            Action.Sell -> data.sell
        }

        val selectedId = when (action) {
            Action.Buy -> selected.buy
            Action.Sell -> selected.sell
        }

        for ((categoryIndex, category) in categories.withIndex()) {
            val list = category.items.filter { !ids.contains(it.title) }
            for ((index, item) in list.withIndex()) {
                if (selectedId?.let { it == item.id } ?: (categoryIndex == 0 && index == 0)) {
                    return item
                }
            }
        }

        return categories[0].items[0]
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun makeMethodsFlow(requestsFlow: Flow<String>): Flow<FiatData> {
        return requestsFlow.flatMapLatest { value ->
            flow {
                App.fiat.getData(value)?.let { emit(it) }
            }
        }
    }
}