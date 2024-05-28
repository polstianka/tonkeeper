package com.tonapps.tonkeeper.ui.screen.buysell

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.settings.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

class FiatAmountViewModel(
    private val settings: SettingsRepository,
    private val ratesRepository: RatesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FiatAmountUiState())
    val uiState: StateFlow<FiatAmountUiState> = _uiState

    init {
        val currency = settings.currency
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(countryCode = settings.country, currency = currency) }
            setValue(0f)
        }
    }

    fun onMethodSelected(id: Int) {
        _uiState.update { state ->
            val newMethodList = state.methodTypes.map { it.copy(selected = it.id == id) }
            state.copy(methodTypes = newMethodList)
        }
    }

    fun reloadCountry() {
        _uiState.update { it.copy(countryCode = settings.country) }
    }

    fun setValue(value: Float) {
        _uiState.update { currentState ->
            currentState.copy(canContinue = false)
        }

        viewModelScope.launch {
            updateValue(value)
        }
    }

    fun updateOperationType(operation: FiatOperation) {
        _uiState.update { it.copy(fiatOperation = operation) }
    }

    fun getSelectedType(): FiatAmountUiState.MethodType {
        return uiState.value.methodTypes.first { it.selected }
    }

    private fun updateValue(newValue: Float) {
        val currency = _uiState.value.currency
        val rates = ratesRepository.getRates(currency, "TON")
        val balanceInCurrency = rates.convert("TON", newValue)
        val min = 0f
        val minFormatted = CurrencyFormatter.format(TokenEntity.TON.symbol, min).toString()

        val minWarning = if (newValue < min && newValue > 0f) minFormatted else ""

        _uiState.update { currentState ->
            currentState.copy(
                rate = CurrencyFormatter.formatFiat(currency.code, balanceInCurrency),
                canContinue = minWarning.isEmpty() && newValue > 0,
                amount = newValue,
                minWarning = minWarning
            )
        }
    }
}

data class FiatAmountUiState(
    val fiatOperation: FiatOperation = FiatOperation.Buy,
    val countryCode: String = "",
    val amount: Float = 0f,
    val currency: WalletCurrency = WalletCurrency.TON,
    val rate: CharSequence = "0 ",
    val minWarning: String = "",
    val canContinue: Boolean = false,
    val methodTypes: List<MethodType> = getDefaultMethods()
) {

    data class MethodType(
        val id: Int,
        val name: String,
        val selected: Boolean,
        val position: ListCell.Position
    ) : BaseListItem()

    companion object {
        const val CREDIT_CARD_ID = 0
        const val CREDIT_CARD_RUB_ID = 1
        const val CRYPTOCURRENCY_ID = 2
        const val APPLE_PAY_ID = 3

        fun getDefaultMethods(): List<MethodType> {
            return listOf(
                MethodType(CREDIT_CARD_ID, "Credit Card", true, ListCell.Position.FIRST),
                MethodType(
                    CREDIT_CARD_RUB_ID,
                    "Credit Card  ·  RUB",
                    false,
                    ListCell.Position.MIDDLE
                ),
                MethodType(CRYPTOCURRENCY_ID, "Cryptocurrency", false, ListCell.Position.MIDDLE),
                MethodType(APPLE_PAY_ID, "Apple Pay", false, ListCell.Position.LAST),
            )
        }
    }
}

@Parcelize
enum class FiatOperation : Parcelable {
    Buy, Sell
}