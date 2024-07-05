

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package io.tonapi.models


import com.squareup.moshi.Json


data class PairList (

    @Json(name = "pairs")
    val pairList: kotlin.collections.List<List<String>>

)