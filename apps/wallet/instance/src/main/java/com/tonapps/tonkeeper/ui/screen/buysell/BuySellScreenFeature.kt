package com.tonapps.tonkeeper.ui.screen.buysell

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tonapps.tonkeeper.api.buysell.BuySellOperator
import com.tonapps.tonkeeper.api.buysell.BuySellType
import com.tonapps.tonkeeper.api.buysell.TradeType
import com.tonapps.wallet.data.core.WalletCurrency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import uikit.mvi.UiFeature

class BuySellScreenFeature :
    UiFeature<BuySellScreenState, BuySellScreenEffect>(BuySellScreenState()) {

    private val _onReadyView: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val onReadyView = _onReadyView.asStateFlow()

    private val _data = MutableLiveData(BuySellData())
    val data: LiveData<BuySellData> = _data

    fun setAmount(am: Float) {
        _data.value = _data.value?.copy(amount = am)
    }

    fun setType(t: BuySellType?) {
        _data.value = _data.value?.copy(buySellType = t)
    }

    fun setOperator(t: BuySellOperator?) {
        _data.value = _data.value?.copy(selectedOperator = t)
    }

    fun setCurrency(t: WalletCurrency) {
        _data.value = _data.value?.copy(currency = t)
    }

    fun setTradeType(t: TradeType) {
        _data.value = _data.value?.copy(tradeType = t)
    }

    fun setCryptoBalance(t: Float) {
        _data.value = _data.value?.copy(cryptoBalance = t)
    }

    fun readyView() {
        _onReadyView.value = true
    }

    fun setHeaderTitle(title: String) {
        updateUiState { it.copy(headerTitle = title) }
    }

    fun setHeaderSubtitle(subtitle: CharSequence?) {
        updateUiState { it.copy(headerSubtitle = subtitle) }
    }

    fun setHeaderVisible(visibility: Boolean) {
        updateUiState { it.copy(headerVisible = visibility) }
    }

    fun nextPage() {
        updateUiState { it.copy(currentPage = it.currentPage + 1) }
    }

    fun prevPage() {
        updateUiState { it.copy(currentPage = it.currentPage - 1) }
    }

    fun setCurrentPage(index: Int) {
        updateUiState { it.copy(currentPage = index) }
    }
}