package com.tonapps.tonkeeper.dialog.trade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.dialog.trade.list.PaymentItem
import com.tonapps.tonkeeper.dialog.trade.list.PaymentType
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.token.TokenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

private const val MIN_AMOUNT = 50.0

class TradeViewModel(
    private val walletRepository: WalletRepository,
    private val tokenRepository: TokenRepository,
) : ViewModel() {
    private val _paymentItemsFlow = MutableStateFlow<List<PaymentItem>>(listOf())
    val paymentItemsFlow = _paymentItemsFlow.asStateFlow()
    private val _minAmountFlow = MutableStateFlow(CurrencyFormatter.format("", MIN_AMOUNT))
    val minAmountFlow = _minAmountFlow.asStateFlow()
    private val _amountFlow = MutableStateFlow<Double?>(MIN_AMOUNT)
    val amountFlow = _amountFlow.asStateFlow()
    private val rateFlow = MutableStateFlow<Double?>(null)
    private val _isContinueButtonActiveFlow = MutableStateFlow(true)
    val isContinueButtonActiveFlow = _isContinueButtonActiveFlow.asStateFlow()
    private val _isBuyMode = MutableStateFlow<Boolean>(true)
    val isBuyMode = _isBuyMode.asStateFlow()

    val convertedFlow =
        _amountFlow.filterNotNull()
            .combine(rateFlow.filterNotNull()) { amount, rate ->
                "${CurrencyFormatter.format("", amount * rate)} USD"
            }

    init {
        fillPaymentMethods()
        loadRate()
    }

    fun selectPaymentType(paymentType: PaymentType) {
        val selectedIndex = paymentItemsFlow.value.indexOfFirst { it.type == paymentType }
        val paymentItems =
            paymentItemsFlow.value.mapIndexed { index, item ->
                item.copy(isSelected = selectedIndex == index)
            }
        _paymentItemsFlow.value = paymentItems
    }

    fun updateAmount(amount: Double) {
        _amountFlow.value = amount
        _isContinueButtonActiveFlow.value = amount >= MIN_AMOUNT
    }

    private fun fillPaymentMethods() {
        _paymentItemsFlow.value =
            listOf(
                PaymentItem(
                    PaymentType.CreditCard,
                    "Credit Card",
                    listOf(R.drawable.mastercard),
                    isSelected = true,
                ),
                PaymentItem(
                    PaymentType.LocalCreditCard("RUB"),
                    "Credit Card  <font color='#818C99'><b>·</b></font>  RUB",
                    listOf(R.drawable.mir),
                ),
                PaymentItem(PaymentType.Crypto, "Cryptocurrency", listOf(R.drawable.cryptos)),
            )
        // _selectedPaymentTypeFlow.value = _paymentTypesFlow.value[0].type
    }

    private fun loadRate() {
        viewModelScope.launch(Dispatchers.IO) {
            val wallet =
                walletRepository.activeWalletFlow.firstOrNull() ?: return@launch
            val token = tokenRepository.get(WalletCurrency("USD"), wallet.address, wallet.testnet)
            val rate = token[0].rateNow
            rateFlow.value = rate.toDouble()
        }
    }

    fun changeBuyMode(clickedTab: Int) {
        _isBuyMode.value = clickedTab == 0
    }
}
