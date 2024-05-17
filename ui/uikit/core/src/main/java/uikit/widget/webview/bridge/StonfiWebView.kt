package uikit.widget.webview.bridge

import android.content.Context
import android.util.AttributeSet

class StonfiWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = android.R.attr.webViewStyle,
) : BridgeWebView(context, attrs, defStyle) {

    init {
        loadUrl("file:///android_asset/stonfi.html")
    }

    fun swapJettonToJetton(
        addressWallet: String,
        offerJettonAddress: String,
        askJettonAddress: String,
        offerAmount: String,
        minAskAmount: String
    ) {
        executeJS("swapJettonToJetton(\"$addressWallet\",\"$offerJettonAddress\",\"$askJettonAddress\", \"$offerAmount\", \"$minAskAmount\");")
    }

    fun swapTonToJetton(
        addressWallet: String,
        askJettonAddress: String,
        offerAmount: String,
        minAskAmount: String
    ) {
        executeJS("swapTonToJetton(\"$addressWallet\",\"$askJettonAddress\", \"$offerAmount\", \"$minAskAmount\");")
    }

    fun swapJettonToTon(
        addressWallet: String,
        offerJettonAddress: String,
        offerAmount: String,
        minAskAmount: String
    ) {
        executeJS("swapJettonToTon(\"$addressWallet\",\"$offerJettonAddress\", \"$offerAmount\", \"$minAskAmount\");")
    }

}