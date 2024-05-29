@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package io.tonapi.models

import com.squareup.moshi.Json


data class OperatorBuyRateList(

    @Json(name = "items")
    val items: kotlin.collections.List<OperatorBuyRate>

)

