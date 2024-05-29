package com.tonapps.tonkeeper.ui.screen.swap.stonfi

import android.util.Log
import com.tonapps.tonkeeper.sign.SignRequestEntity
import org.json.JSONArray
import uikit.widget.webview.bridge.JsBridge

class StonfiBridge2(
    val address: String,
    val close: () -> Unit,
    val sendTransaction: suspend (request: SignRequestEntity) -> String?
): JsBridge("tonkeeperStonfi") {

    override val availableFunctions = arrayOf("close", "sendTransaction")

    init {
        keys["address"] = address
    }

    override suspend fun invokeFunction(name: String, args: JSONArray): Any? {
        if (name == "close") {
            close()
            return null
        } else if (name == "sendTransaction" && args.length() == 1) {
            Log.d("DAppScreenLog", "sendTransaction: ${args}")
            val request = SignRequestEntity(args.getJSONObject(0))
            return sendTransaction(request)
        }
        throw IllegalArgumentException("Unknown function: $name")
    }

}