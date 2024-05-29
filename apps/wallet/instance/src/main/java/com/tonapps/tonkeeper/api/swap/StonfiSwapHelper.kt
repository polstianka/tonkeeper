package com.tonapps.tonkeeper.api.swap

import android.util.Log
import com.tonapps.extensions.toByteArray
import com.tonapps.security.Security
import com.tonapps.security.hex
import com.tonapps.tonkeeper.sign.SignRequestEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import uikit.widget.webview.bridge.BridgeWebView
import uikit.widget.webview.bridge.JsBridge
import java.math.BigInteger

class StonfiSwapHelper(
    private val bridgeWebView: BridgeWebView,
    private val doOnClose: () -> Unit,
    private val sendTransaction: suspend (request: SignRequestEntity) -> String?
) : JsBridge("tonkeeperStonfi") {

    companion object {
        const val STONFI_SDK_PAGE = "file:///android_asset/stonfi_swap.html"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        scope.launch {
            withContext(Dispatchers.Main) {
                bridgeWebView.jsBridge = this@StonfiSwapHelper
            }
        }
    }

    override val availableFunctions: Array<String>
        get() = arrayOf("sendTransaction")

    override suspend fun invokeFunction(name: String, args: JSONArray): Any? {
        if (name == "sendTransaction" && args.length() == 1) {
            Log.d("DAppScreenLog", "sendTransaction: $args")
            val request = SignRequestEntity(args.getJSONObject(0))
            val result = sendTransaction(request)
            Log.d("DAppScreenLog", "sendTransaction result: $result")
            scope.launch {
                delay(500)
                withContext(Dispatchers.Main) {
                    doOnClose()
                }
            }
        }
        return null
    }

    fun jettonToJetton(walletAddress: String, jettonAddress: String, askJettonAddress: String, units: String, minAskUnits: String, queryId: String = getQueryId()) {
        bridgeWebView.executeJS(
            """
                jettonToJetton(
                    "$walletAddress",
                    "$jettonAddress",
                    "$askJettonAddress",
                    "$units",
                    "$minAskUnits",
                    "$queryId"
                )
            """.trimIndent()
        )
    }

    fun tonToJetton(walletAddress: String, jettonAddress: String, units: String, minAskUnits: String, queryId: String = getQueryId()) {
        bridgeWebView.executeJS(
            """
                tonToJetton(
                    "$walletAddress",
                    "$jettonAddress",
                    "$units",
                    "$minAskUnits",
                    "$queryId"
                )
            """.trimIndent()
        )
    }

    fun jettonToTon(walletAddress: String, jettonAddress: String, units: String, minAskUnits: String, queryId: String = getQueryId()) {
        bridgeWebView.executeJS(
            """
                jettonToTon(
                    "$walletAddress",
                    "$jettonAddress",
                    "$units",
                    "$minAskUnits",
                    "$queryId"
                )
            """.trimIndent()
        )
    }

    fun getQueryId(): String {
        try {
            val tonkeeperSignature = 0x546de4ef.toByteArray()
            val randomBytes = Security.randomBytes(4)
            val value = tonkeeperSignature + randomBytes
            val hexString = hex(value)
            return BigInteger(hexString, 16).toString()
        } catch (e: Throwable) {
            return BigInteger.ZERO.toString()
        }
    }
}