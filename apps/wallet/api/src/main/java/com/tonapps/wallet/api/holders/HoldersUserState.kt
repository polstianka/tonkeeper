package com.tonapps.wallet.api.holders

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi

data class HoldersUserState(
    @Json(name = "ok") val ok: Boolean,
    @Json(name = "state") val state: Map<String, Any>?
) {
    fun toJSON(): String {
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(Map::class.java)
        val json = adapter.toJson(state)

        return json
    }
}
