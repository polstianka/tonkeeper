package com.tonapps.tonkeeper.api.internal

import com.tonapps.network.Network
import org.json.JSONObject

object Tonkeeper {

    const val HOST = "api.tonkeeper.com"

    private fun endpoint(path: String): String {
        return "https://${HOST}/$path?lang=en&build=3.5.0&chainName=mainnet&platform=android"
    }

    private fun endpoint(path: String, lang: String): String {
        return "https://${HOST}/$path?lang=${lang}&build=3.5.0&chainName=mainnet&platform=android"
    }

    fun get(path: String): JSONObject {
        val url = endpoint(path)
        val body = Network.get(url)
        return JSONObject(body)
    }

    fun get(path: String, lang: String): JSONObject {
        val url = endpoint(path, lang)
        val body = Network.get(url)
        return JSONObject(body)
    }
}