package com.tonapps.wallet.api.entity

import android.net.Uri
import android.os.Parcelable
import com.tonapps.blockchain.ton.extensions.toUserFriendly
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
        @Deprecated("""
            Consider dropping special address value for TON,
            as it requires adding extra handling everywhere TokenEntity is used.
            
            Rely only on proper address value,
            or, better, use special type with cache for raw & user-friendly addresses.
        """)
        val TON = TokenEntity(
            address = "TON",
            name = "Toncoin",
            symbol = "TON",
            imageUri = Uri.Builder().scheme("res").path(R.drawable.ic_ton_with_bg.toString()).build(),
            decimals = 9,
            verification = Verification.whitelist
        )

        const val TON_CONTRACT_USER_FRIENDLY_ADDRESS = "EQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAM9c"
        const val TON_CONTRACT_RAW_ADDRESS = "0:0000000000000000000000000000000000000000000000000000000000000000"

        private fun convertVerification(verification: JettonVerificationType): Verification {
            return when (verification) {
                JettonVerificationType.whitelist -> Verification.whitelist
                JettonVerificationType.blacklist -> Verification.blacklist
                else -> Verification.none
            }
        }

        // TODO(API): this should not be hardcoded

        const val USDT_SYMBOL = "USD₮"
        const val USDT_CONTRACT_RAW_ADDRESS = "0:b113a994b5024a16719f69139328eb759596c38a25f59028b146fecdc3621dfe"

        fun specialSymbol(symbol: String, address: String): String? {
            return if (isUSDT(symbol, address)) {
                TON.symbol
            } else {
                null
            }
        }

        fun isUSDT(symbol: String, address: String): Boolean {
            return symbol == USDT_SYMBOL && address == USDT_CONTRACT_RAW_ADDRESS
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

    fun getRawAddress(): String {
        return if (isTon) {
            TON_CONTRACT_RAW_ADDRESS
        } else {
            address
        }
    }

    fun getUserFriendlyAddress(wallet: Boolean, testnet: Boolean): String {
        return if (isTon) {
            TON_CONTRACT_USER_FRIENDLY_ADDRESS
        } else {
            address.toUserFriendly(wallet, testnet)
        }
    }
}