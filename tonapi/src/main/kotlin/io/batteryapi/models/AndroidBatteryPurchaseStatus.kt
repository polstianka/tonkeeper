/**
 *
 * Please note:
 * This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 *
 */

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package io.batteryapi.models

import io.batteryapi.models.AndroidBatteryPurchaseStatusPurchasesInner

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param purchases 
 */


data class AndroidBatteryPurchaseStatus (

    @Json(name = "purchases")
    val purchases: kotlin.collections.List<AndroidBatteryPurchaseStatusPurchasesInner>

) {


}
