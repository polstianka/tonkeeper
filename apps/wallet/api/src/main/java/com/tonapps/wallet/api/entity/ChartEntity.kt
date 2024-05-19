package com.tonapps.wallet.api.entity

import org.json.JSONArray
import org.json.JSONObject

data class ChartEntity(
    val x: Long,
    val y: Float
) {

    constructor(json: JSONObject) : this(
        json.getLong("x"),
        json.getDouble("y").toFloat()
    )

    constructor(array: JSONArray) : this(
        array.getLong(0),
        array.getDouble(1).toFloat()
    )
}