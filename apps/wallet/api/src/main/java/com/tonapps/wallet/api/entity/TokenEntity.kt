package com.tonapps.wallet.api.entity

import android.net.Uri
import android.os.Parcelable
import com.tonapps.wallet.api.R
import io.tonapi.models.JettonPreview
import io.tonapi.models.JettonVerificationType
import kotlinx.parcelize.Parcelize

@Parcelize
data class TokenEntity(
    val address: String,
    val name: String,
    val symbol: String,
    val imageUri: Uri,
    val decimals: Int,
    val verification: Verification
): Parcelable {

    enum class Verification {
        whitelist, blacklist, none
    }

    companion object {

        val TON_ICON_URI = Uri.Builder().scheme("res").path(R.drawable.ic_ton_with_bg.toString()).build()

        val TON = TokenEntity(
            address = "TON",
            name = "Toncoin",
            symbol = "TON",
            imageUri = TON_ICON_URI,
            decimals = 9,
            verification = Verification.whitelist
        )

        private fun convertVerification(verification: JettonVerificationType): Verification {
            return when (verification) {
                JettonVerificationType.whitelist -> Verification.whitelist
                JettonVerificationType.blacklist -> Verification.blacklist
                else -> Verification.none
            }
        }
    }

    val isTon: Boolean
        get() = address == TON.address

    constructor(jetton: JettonPreview) : this(
        address = jetton.address,
        name = jetton.name,
        symbol = jetton.symbol,
        imageUri = Uri.parse(jetton.image),
        decimals = jetton.decimals,
        verification = convertVerification(jetton.verification)
    )
}