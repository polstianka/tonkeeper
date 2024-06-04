package com.tonapps.wallet.api.entity

import android.net.Uri
import android.os.Parcelable
import com.tonapps.blockchain.ton.extensions.toRawAddress
import com.tonapps.blockchain.ton.extensions.toUserFriendly
import com.tonapps.wallet.api.R
import io.stonfi.models.AssetInfoSchema
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
        val TETHER_USDT_ADDRESS = "0:b113a994b5024a16719f69139328eb759596c38a25f59028b146fecdc3621dfe"
        val BRIDGED_JUSDT_ADDRESS = "0:729c13b6df2c07cbf0a06ab63d34af454f3d320ec1bcd8fb5c6d24d0806a17c2"

        val TON = TokenEntity(
            address = "TON",
            name = "Toncoin",
            symbol = "TON",
            imageUri = Uri.Builder().scheme("res").path(R.drawable.ic_ton_with_bg.toString()).build(),
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

    val isTetherUsdt: Boolean
        get() = address == TETHER_USDT_ADDRESS

    val isBridgedTetherUsdt: Boolean
        get() = address == BRIDGED_JUSDT_ADDRESS

    constructor(jetton: JettonPreview) : this(
        address = jetton.address,
        name = jetton.name,
        symbol = jetton.symbol,
        imageUri = Uri.parse(jetton.image),
        decimals = jetton.decimals,
        verification = convertVerification(jetton.verification)
    )

    constructor(jetton: AssetInfoSchema) : this(
        address = if (jetton.contractAddress == "EQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAM9c") "TON" else jetton.contractAddress.toRawAddress(),
        name = jetton.displayName ?: "",
        symbol = jetton.symbol,
        imageUri = try {
            if (jetton.contractAddress == "EQC98_qAmNEptUtPc7W6xdHh_ZHrBUFpw5Ft_IzNU20QAJav")
                Uri.Builder().scheme("res").path(com.tonapps.uikit.icon.R.drawable.ic_staking_tonstakers.toString()).build()
            else Uri.parse(jetton.imageUrl)
        } catch (t: Throwable) {
           Uri.EMPTY
       },
        decimals = jetton.decimals,
        verification = if (jetton.blacklisted) Verification.blacklist else Verification.whitelist
    )

    val stonFiAddress: String get() =
        if (isTon) "EQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAM9c" else address.toUserFriendly(false, false)

    val stonFiProxyAddress: String get() =
        if (isTon) "EQCM3B12QK1e4yZSf8GtBRT0aLMNyEsBc_DhVfRRtOEffLez" else address.toUserFriendly(false, false)
}