package com.tonapps.network

import org.json.JSONObject

data class WebSocketEvent(
    val data: String,
) {
    val json: JSONObject by lazy {
        try {
            JSONObject(data)
        } catch (e: Throwable) {
            JSONObject()
        }
    }
}