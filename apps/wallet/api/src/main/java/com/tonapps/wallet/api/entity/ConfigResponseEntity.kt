package com.tonapps.wallet.api.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
data class ConfigResponseEntity(
    val mainnet: ConfigEntity,
    val testnet: ConfigEntity,
): Parcelable {
    constructor(json: JSONObject, debug: Boolean) : this(
        mainnet = ConfigEntity(json.getJSONObject("mainnet"), debug),
        testnet = ConfigEntity(json.getJSONObject("testnet"), debug)
    )
}
