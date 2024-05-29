package com.tonapps.tonkeeper.ui.screen.buysell.confirm

import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.api.buysell.BuySellRepository
import com.tonapps.tonkeeper.api.buysell.TYPE_CREDIT
import com.tonapps.tonkeeper.api.buysell.TYPE_CRYPTO
import com.tonapps.tonkeeper.api.buysell.TYPE_GPAY
import com.tonapps.tonkeeper.api.buysell.TradeType
import com.tonapps.tonkeeper.core.fiat.models.FiatSuccessUrlPattern
import com.tonapps.tonkeeper.ui.screen.buysell.BuySellData
import com.tonapps.wallet.localization.Localization
import core.QueueScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uikit.mvi.UiFeature

class BuySellConfirmScreenFeature(
    private val buySellRepository: BuySellRepository
) : UiFeature<BuySellConfirmScreenState, BuySellConfirmScreenEffect>(BuySellConfirmScreenState()) {

    private val queueScope = QueueScope(Dispatchers.IO)

    fun setData(data: BuySellData) {

        updateUiState { currentState ->
            currentState.copy(
                tradeType = data.tradeType,
                cryptoBalance = data.cryptoBalance,
                buySellType = data.buySellType,
                selectedOperator = data.selectedOperator,
                currency = data.currency,
                amountCrypto = data.amount,
                loading = false,
                rate = App.instance.getString(Localization.rate_for, "${"%.2f".format( data.selectedOperator?.rate ).replace(',', '.')} ${data.currency.code}", "1 TON"),
                canContinue = true,
                error1 = false,
                error2 = false
            )
        }
    }

    private suspend fun getMerchantUrl(): String {
        var url = uiState.value.selectedOperator?.actionUrl ?: ""
        val wallet = App.walletManager.getWalletInfo() ?: return ""
        val address = wallet.address
        var paytype: String? = null
        // additional hardcode params: amount and so on - should be implemented on backend side
        url = when (uiState.value.selectedOperator?.id) {
            BuySellRepository.MERCURYO_MERCHANT -> {
                paytype = when (uiState.value.buySellType?.id) {
                    TYPE_CREDIT -> "card"
                    else -> null
                }
                "$url&amount={AMOUNT_TON}&payment_method={PAYMENT_TYPE}&fix_payment_method=true"
            }
            BuySellRepository.TRANSAK_MERCHANT -> {
                paytype = when (uiState.value.buySellType?.id) {
                    TYPE_CREDIT -> "credit_debit_card"
                    TYPE_GPAY -> "google_pay"
                    else -> null
                }
                if (uiState.value.tradeType == TradeType.BUY) {
                    "$url&fiatAmount={AMOUNT_FIAT}&paymentMethod={PAYMENT_TYPE}&fiatCurrency={CUR_FROM}"
                } else {
                    "$url&cryptoAmount={AMOUNT_TON}&paymentMethod={PAYMENT_TYPE}&fiatCurrency={CUR_FROM}"
                }
            }
            BuySellRepository.MOONPAY_MERCHANT -> {
                paytype = when (uiState.value.buySellType?.id) {
                    TYPE_CREDIT -> "credit_debit_card"
                    else -> null
                }
                "$url&baseCurrencyAmount={AMOUNT_FIAT}&paymentMethod={PAYMENT_TYPE}"
            }
            BuySellRepository.DREAMWALKERS_MERCHANT -> {
                url // did not find custom params
            }
            else -> url
        }
        val rate = uiState.value.selectedOperator?.rate ?: 1f
        val fiat = uiState.value.amountCrypto * rate
        val fiatStr = "%.2f".format( fiat ).replace(',', '.')

        return App.fiat.replaceUrl(url, address, uiState.value.currency.code, uiState.value.tradeType.type, uiState.value.amountCrypto.toString(), fiatStr, paytype)
    }

    fun setValue(youPay: String) {
        val youPayF = youPay.toFloatOrNull() ?: 0f
        val rate = uiState.value.selectedOperator?.rate ?: 1f
        if (uiState.value.tradeType == TradeType.BUY) {
            val crypto = youPayF / rate
            updateUiState { currentState ->
                currentState.copy(
                    amountCrypto = crypto,
                    canContinue = crypto >= 5f,
                    error2 = crypto < 5f,
                    error1 = false
                )
            }
        } else {
            updateUiState { currentState ->
                currentState.copy(
                    amountCrypto = youPayF,
                    canContinue = youPayF >= 5f && youPayF < uiState.value.cryptoBalance,
                    error1 = youPayF < 5f || youPayF > uiState.value.cryptoBalance,
                    error2 = false
                )
            }
        }
    }

    fun setValue2(youGet: String) {
        val youGetF = youGet.toFloatOrNull() ?: 0f
        val rate = uiState.value.selectedOperator?.rate ?: 1f
        if (uiState.value.tradeType == TradeType.BUY) {
            updateUiState { currentState ->
                currentState.copy(
                    amountCrypto = youGetF,
                    canContinue = youGetF >= 5f,
                    error2 = youGetF < 5f,
                    error1 = false
                )
            }
        } else {
            val crypto = youGetF / rate
            updateUiState { currentState ->
                currentState.copy(
                    amountCrypto = crypto,
                    canContinue = crypto >= 5f && crypto < uiState.value.cryptoBalance,
                    error1 = crypto < 5f || crypto > uiState.value.cryptoBalance,
                    error2 = false
                )
            }
        }
    }

    fun operate() {
        updateUiState { currentState ->
            currentState.copy(
                loading = true
            )
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val merchantUrl = getMerchantUrl()

                updateUiState { currentState ->
                    currentState.copy(
                        url = merchantUrl
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        queueScope.cancel()
    }
}