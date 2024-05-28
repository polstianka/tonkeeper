package com.tonapps.tonkeeper.fragment.swap.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TokenInfo(
    val name: String,
    val symbol: String,
    val balance: String,
    val decimals: Int,
    val balanceFiat: String,
    val iconUri: Uri,
    val contractAddress: String,
    val priority: Int,
    val tonTag: Boolean = false
) : Parcelable {
    val isTon: Boolean
        get() = contractAddress == "TON" || contractAddress == "EQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAM9c"
}