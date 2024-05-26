package com.tonapps.tonkeeper.dialog.trade.operator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.core.fiat.models.FiatItem
import com.tonapps.tonkeeper.core.fiat.models.FiatSuccessUrlPattern
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ChooseOperatorViewModel(
    private val settings: SettingsRepository,
    private val api: API,
) : ViewModel() {
    private val _itemsFlow = MutableStateFlow<List<OperatorItem>>(listOf())
    val itemsFlow = _itemsFlow.asStateFlow()
    private val _isLoadingFlow = MutableStateFlow<Boolean>(true)
    val isLoadingFlow = _isLoadingFlow.asStateFlow()
    private var selectedItemId: String = ""
    private val _openUrlChannel = Channel<Pair<String, FiatSuccessUrlPattern?>>()
    val openUrlFlow = _openUrlChannel.receiveAsFlow()
    private val _showConfirmationDialogChannel = Channel<FiatItem>()
    val showConfirmDialogFlow = _showConfirmationDialogChannel.receiveAsFlow()
    private val _showConfirmationScreenChannel = Channel<OperatorItem>()
    val showConfirmationScreenFlow = _showConfirmationScreenChannel.receiveAsFlow()
    private val fiatItemsMap = HashMap<String, FiatItem>()

    val itemsPresentFlow =
        itemsFlow.map {
            it.isNotEmpty()
        }

    fun load(
        shouldBuy: Boolean,
        currency: String,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            _isLoadingFlow.value = true
            fiatItemsMap.clear()
            val tradeRates = api.loadTradeRates(currency)
            val country = settings.country
            val methods = App.fiat.getMethods(country, shouldBuy)
            val items = mutableListOf<OperatorItem>()
            methods.forEach { fiatItem ->
                val rate =
                    tradeRates.firstOrNull { it.name.lowercase() == fiatItem.title.lowercase() }
                val isSelected = items.size == 0
                selectedItemId = fiatItem.id
                fiatItemsMap[fiatItem.id] = fiatItem
                items.add(
                    OperatorItem(
                        id = fiatItem.id,
                        title = fiatItem.title,
                        paymentUrl = fiatItem.actionButton.url,
                        iconUrl = fiatItem.iconUrl,
                        subtitle = fiatItem.subtitle,
                        successUrlPattern = fiatItem.successUrlPattern,
                        rate = rate?.rate,
                        isSelected = isSelected,
                        fiatCurrency = currency,
                        minTonBuyAmount = rate?.minTonBuyAmount ?: 0.0,
                        minTonSellAmount = rate?.minTonSellAmount ?: 0.0,
                    ),
                )
            }
            if (methods.isEmpty()) {
                selectedItemId = ""
            }
            _itemsFlow.value = items
            _isLoadingFlow.value = false
        }
    }

    fun checkItem(itemId: String) {
        selectedItemId = itemId
        _itemsFlow.value =
            _itemsFlow.value.map {
                it.copy(isSelected = it.id == selectedItemId)
            }
    }

    fun onContinueButtonClicked() {
        viewModelScope.launch {
            val selectedItem = itemsFlow.value.firstOrNull { it.id == selectedItemId }
            if (isShowConfirmation(selectedItemId)) {
                selectedItem?.let {
                    if (it.rate == null || it.rate == 0.0) {
                        _showConfirmationDialogChannel.send(fiatItemsMap[selectedItemId]!!)
                    } else {
                        _showConfirmationScreenChannel.send(it)
                    }
                }
            } else {
                selectedItem?.let {
                    _openUrlChannel.send(selectedItem.paymentUrl to selectedItem.successUrlPattern)
                }
            }
        }
    }

    private suspend fun isShowConfirmation(id: String): Boolean {
        return App.fiat.isShowConfirmation(id)
    }
}
