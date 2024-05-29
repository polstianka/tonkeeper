package com.tonapps.tonkeeper.dialog.trade.operator.confirmation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.dialog.trade.operator.OperatorItem
import com.tonapps.wallet.data.core.WalletCurrency
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
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
    private var isBuyMethod = true
    private var operatorItem: OperatorItem? = null

    fun submitOperatorItem(
        item: OperatorItem,
        isBuyMethod: Boolean,
    ) {
        this.operatorItem = item
        this.isBuyMethod = isBuyMethod
        rate =
            if (isBuyMethod) {
                item.rate ?: 0.0
            } else {
                item.rate?.let {
                    (1 / it)
                } ?: 0.0
            }
        if (isBuyMethod) {
            _payCurrencyFlow.value = item.fiatCurrency
            _getCurrencyFlow.value = WalletCurrency.TON.code
        } else {
            _payCurrencyFlow.value = WalletCurrency.TON.code
            _getCurrencyFlow.value = item.fiatCurrency
        }
    }

    fun getContinueButtonAvailableFlow() =
        _paymentInfoFlow.map {
            if (isBuyMethod) {
                return@map it.get >= (operatorItem?.minTonBuyAmount ?: 0.0)
            } else {
                return@map it.pay >= (operatorItem?.minTonSellAmount ?: 0.0)
            }
        }

    fun onPayChanged(sum: Double) {
        viewModelScope.launch {
            _paymentInfoFlow.emit(
                PaymentInfo(
                    pay = sum,
                    get = sum / rate,
                ),
            )
        }
    }

    fun onReceiveChanged(sum: Double) {
        viewModelScope.launch {
            _paymentInfoFlow.emit(
                PaymentInfo(
                    pay = sum * rate,
                    get = sum,
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
