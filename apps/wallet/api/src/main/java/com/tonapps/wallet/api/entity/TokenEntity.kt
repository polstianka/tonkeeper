package com.tonapps.wallet.api.entity

import android.net.Uri
import android.os.Parcelable
import com.tonapps.wallet.api.R
import io.tonapi.models.JettonPreview
import io.tonapi.models.JettonVerificationType
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ton.block.MsgAddressInt

@Parcelize
@Serializable
data class TokenEntity(
    val address: String,
    val name: String,
    val symbol: String,
    @Serializable(MyUriSerializer::class)
    val imageUri: Uri,
    val decimals: Int,
    val verification: Verification
): Parcelable {

    enum class Verification {
        whitelist, blacklist, none
    }

    companion object {
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
        get() = address == TON.address || symbol == TON.symbol

    constructor(jetton: JettonPreview) : this(
        address = jetton.address,
        name = jetton.name,
        symbol = jetton.symbol,
        imageUri = Uri.parse(jetton.image),
        decimals = jetton.decimals,
        verification = convertVerification(jetton.verification)
    )

    fun hasTheSameAddress(another: TokenEntity): Boolean {
        return when {
            isTon && another.isTon -> true
            isTon || another.isTon -> false
            else -> address.isAddressEqual(another.address)
        }
    }
}


private class MyUriSerializer : KSerializer<Uri> {
    override val descriptor: SerialDescriptor
        get() = serialDescriptor<Uri>()

    override fun deserialize(decoder: Decoder): Uri {
        val string = decoder.decodeString()
        return Uri.parse(string)
    }

    override fun serialize(encoder: Encoder, value: Uri) {
        encoder.encodeString(value.toString())
    }

}

fun String.isAddressEqual(another: String): Boolean {
    return MsgAddressInt.parse(this).isAddressEqual(another)
}

fun MsgAddressInt.isAddressEqual(another: String): Boolean {
    return try {
        this == MsgAddressInt.parse(another)
    } catch (_: IllegalArgumentException) {
        false
    }
}