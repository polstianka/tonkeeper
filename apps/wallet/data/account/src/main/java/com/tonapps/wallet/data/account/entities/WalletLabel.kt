package com.tonapps.wallet.data.account.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WalletLabel(
    val accountName: String,
    val emoji: CharSequence,
    val color: Int
): Parcelable {

    val isEmpty: Boolean
        get() = accountName.isBlank() && emoji.isBlank()

    val name: String
        get() = accountName.ifBlank { "Wallet" }

    val title: CharSequence?
        get() = if (isEmpty) null else String.format("%s %s", emoji, name)
}