package com.tonapps.tonkeeper.ui.screen.buysell.operator

import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.api.buysell.BuySellOperator
import com.tonapps.tonkeeper.api.buysell.BuySellRepository
import com.tonapps.tonkeeper.api.buysell.TradeType
import com.tonapps.tonkeeper.ui.screen.buysell.BuySellData
import com.tonapps.tonkeeper.ui.screen.swap.SwapData
import com.tonapps.wallet.data.core.WalletCurrency
import core.QueueScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uikit.mvi.UiFeature

class BuySellOperatorScreenFeature(
    private val buySellRepository: BuySellRepository
) : UiFeature<BuySellOperatorScreenState, BuySellOperatorScreenEffect>(BuySellOperatorScreenState()) {

    private val queueScope = QueueScope(Dispatchers.IO)

    private suspend fun loadData(prevSelected: BuySellOperator?) = withContext(Dispatchers.IO) {
        val types = buySellRepository.getOperators(uiState.value.currency.code, uiState.value.tradeType).sortedBy { it.rate }
        var selectedType: BuySellOperator? = null
        if (types.find { it.id == prevSelected?.id } != null) {
            selectedType = prevSelected
        } else {
            selectedType = if (types.isNotEmpty()) types[0] else null
        }
        updateUiState { state ->
            state.copy(
                operators = types.mapIndexed { index, buySellOperator ->  buySellOperator.copy(selected = buySellOperator.id == selectedType?.id, best = index == 0) },
                selectedOperator = selectedType,
                loading = false
            )
        }
    }

    fun setData(data: BuySellData) {
        val prevSelected = uiState.value.selectedOperator
        updateUiState { currentState ->
            currentState.copy(
                tradeType = data.tradeType,
                buySellType = data.buySellType,
                currency = data.currency,
                selectedOperator = null,
                loading = true
            )
        }

        viewModelScope.launch {
            loadData(prevSelected)
        }
    }

    fun selectOperator(type: BuySellOperator) {
        updateUiState { state ->
            state.copy(
                operators = state.operators.map { it.copy(selected = it.id == type.id) },
                selectedOperator = type
            )
        }
    }

    fun setCurrency(currency: WalletCurrency) {

    }

    override fun onCleared() {
        super.onCleared()
        queueScope.cancel()
    }
}