package com.tonapps.tonkeeper.ui.screen.swap.view.swapView

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView
import android.webkit.WebViewClient

class SwapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
): WebView(context, attrs, defStyle) {

    init {
        // Включаем поддержку JavaScript
        settings.javaScriptEnabled = true
        // Настройка WebViewClient для обработки загрузки страниц
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // После загрузки страницы, устанавливаем параметры и вызываем JavaScript-метод
                val offerAmount = "1000000000" // Передаем значение по умолчанию
                val askJettonAddress = "EQA2kCVNwVsil2EM2mB0SkXytxCqQjS4mttjDpnXmwG9T6bO" // Передаем значение по умолчанию
                executeSDKMethod(offerAmount, askJettonAddress)
            }
        }
    }

    // Метод для установки значения offerAmount
    fun setOfferAmount(offerAmount: String) {
        val jsCommand = "setOfferAmount('$offerAmount')"
        evaluateJavascript(jsCommand, null)
    }

    // Метод для установки значения askJettonAddress
    fun setAskJettonAddress(askJettonAddress: String) {
        val jsCommand = "setAskJettonAddress('$askJettonAddress')"
        evaluateJavascript(jsCommand, null)
    }

    // Пример JavaScript-кода для взаимодействия с SDK с передачей параметров из приложения
    @SuppressLint("SetJavaScriptEnabled")
    private fun executeSDKMethod(offerAmount: String, askJettonAddress: String) {
        val javascript = """
            function executeSDKMethod(offerAmount, askJettonAddress) {
                // Взаимодействие с SDK с использованием переданных параметров
                const txParams = await router.buildSwapTonToJettonTxParams({
                    userWalletAddress: "UQBZH1jb6opuqZAgxqUAScv50nda_yQkRO8MW6vz8rqe0xcw", 
                    proxyTonAddress: pTON.v1.address,
                    offerAmount: new TonWeb.utils.BN(offerAmount), // Переданный параметр offerAmount
                    askJettonAddress: askJettonAddress, // Переданный параметр askJettonAddress
                    minAskAmount: new TonWeb.utils.BN("1"),
                    queryId: 12345,
                });

                // Возвращаем результат как JSON-строку
                return JSON.stringify({
                    to: txParams.to,
                    amount: txParams.gasAmount,
                    payload: txParams.payload,
                });
            }
        """.trimIndent()

        // Загрузка JavaScript-кода
        loadUrl("javascript:$javascript")
    }
}