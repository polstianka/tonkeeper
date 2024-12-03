package com.tonapps.wallet.api.holders

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import kotlinx.parcelize.Parcelize

@Parcelize
data class HoldersCardEntity(
    @Json(name = "id") val id: String,
    @Json(name = "type") val type: String,
    @Json(name = "status") val status: String,
    @Json(name = "walletId") val walletId: String?,
    @Json(name = "fiatCurrency") val fiatCurrency: String,
    @Json(name = "fiatBalance") val fiatBalance: String?,
    @Json(name = "lastFourDigits") val lastFourDigits: String?,
    @Json(name = "productId") val productId: String,
    @Json(name = "personalizationCode") val personalizationCode: String,
    @Json(name = "seed") val seed: String?,
    @Json(name = "updatedAt") val updatedAt: String,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "provider") val provider: String?,
    @Json(name = "kind") val kind: String?
): Parcelable {
    fun toJSON(): String {
        val moshi = Moshi.Builder()
            .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .build()
        val adapter = moshi.adapter(HoldersCardEntity::class.java)
        return adapter.toJson(this)
    }
}