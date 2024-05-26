package com.tonapps.tonkeeper.dialog.trade.operator.confirmation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.dialog.trade.operator.OperatorItem
import com.tonapps.wallet.data.core.WalletCurrency
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ConfirmationTradeViewModel : ViewModel() {
    private val _payCurrencyFlow = MutableStateFlow("")
    val payCurrencyFlow = _payCurrencyFlow.asStateFlow()
    private val _getCurrencyFlow = MutableStateFlow("")
    val getCurrencyFlow = _getCurrencyFlow.asStateFlow()
    private val _paymentInfoFlow = MutableSharedFlow<PaymentInfo>()
    val paymentInfoFlow = _paymentInfoFlow.asSharedFlow()
    private val _continueFlow = Channel<ContinuePaymentItem>()
    val continueFlow = _continueFlow.receiveAsFlow()

    private var rate = 0.0
    private var isPayMethod = true
    private var operatorItem: OperatorItem? = null

    fun submitOperatorItem(
        item: OperatorItem,
        isPayMethod: Boolean,
    ) {
        this.operatorItem = item
        this.isPayMethod = isPayMethod
        rate =
            if (isPayMethod) {
                item.rate ?: 0.0
            } else {
                item.rate?.let {
                    (1 / it)
                } ?: 0.0
            }
        if (isPayMethod) {
            _payCurrencyFlow.value = item.fiatCurrency
            _getCurrencyFlow.value = WalletCurrency.TON.code
        } else {
            _payCurrencyFlow.value = WalletCurrency.TON.code
            _getCurrencyFlow.value = item.fiatCurrency
        }
    }

    fun onPayChanged(sum: Double) {
        viewModelScope.launch {
            _paymentInfoFlow.emit(
                PaymentInfo(
                    pay = CurrencyFormatter.format("", sum),
                    get = CurrencyFormatter.format("", sum / rate),
                ),
            )
        }
    }

    fun onReceiveChanged(sum: Double) {
        viewModelScope.launch {
            _paymentInfoFlow.emit(
                PaymentInfo(
                    pay = CurrencyFormatter.format("", sum * rate),
                    get = CurrencyFormatter.format("", sum),
                ),
            )
        }
    }

    fun onContinueClicked() {
        operatorItem?.let { item ->
            viewModelScope.launch {
                _continueFlow.send(
                    ContinuePaymentItem(
                        url = item.paymentUrl,
                        pattern = item.successUrlPattern,
                    ),
                )
            }
        }
    }
}
