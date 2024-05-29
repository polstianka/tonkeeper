package com.tonapps.tonkeeper.api.chart

import org.json.JSONObject

data class ChartEntity(
    val x: Long,
    val y: Float
) {

    constructor(json: JSONObject) : this(
        json.getLong("x"),
        json.getDouble("y").toFloat()
    )
}