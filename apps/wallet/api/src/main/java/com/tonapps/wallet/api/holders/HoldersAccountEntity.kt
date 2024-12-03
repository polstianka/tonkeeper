package com.tonapps.wallet.api.holders

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import kotlinx.parcelize.Parcelize

@Parcelize
data class HoldersAccountEntity(
    @Json(name = "id") val id: String,
    @Json(name = "accountIndex") val accountIndex: Int,
    @Json(name = "address") val address: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "seed") val seed: String?,
    @Json(name = "state") val state: String,
    @Json(name = "balance") val balance: String,
    @Json(name = "tzOffset") val tzOffset: Int,
    @Json(name = "contract") val contract: String,
    @Json(name = "partner") val partner: String,
    @Json(name = "network") val network: String,
    @Json(name = "ownerAddress") val ownerAddress: String,
    @Json(name = "cryptoCurrency") val cryptoCurrency: CryptoCurrency?,
    @Json(name = "limits") val limits: AccountLimits?,
    @Json(name = "cards") val cards: List<HoldersCardEntity>
): Parcelable {
    @Parcelize
    data class CryptoCurrency(
        val decimals: Int,
        val ticker: String,
        val tokenContract: String?
    ): Parcelable

    @Parcelize
    data class AccountLimits(
        val tzOffset: Int,
        val dailyDeadline: Int,
        val dailySpent: String,
        val monthlyDeadline: Int,
        val monthlySpent: String,
        val monthly: String,
        val daily: String,
        val onetime: String
    ): Parcelable

    fun toJSON(): String {
        val moshi = Moshi.Builder()
            .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .build()
        val adapter = moshi.adapter(HoldersAccountEntity::class.java)
        return adapter.toJson(this)
    }
}
