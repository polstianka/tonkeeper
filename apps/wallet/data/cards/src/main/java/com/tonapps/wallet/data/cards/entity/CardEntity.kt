package com.tonapps.wallet.data.cards.entity

import android.os.Parcelable
import com.tonapps.icu.Coins
import kotlinx.parcelize.Parcelize

@Parcelize
data class CardEntity(
    val type: Type,
    val id: String,
    val accountId: String,
    val balance: Coins,
    val currency: String,
    val fiat: Coins,
    val prepaidBalance: Coins? = null,
    val prepaidCurrency: String? = null,
    val lastFourDigits: String,
    val kind: CardKind,
) : Parcelable {
    enum class Type {
        ACCOUNT,
        PREPAID
    }
}
