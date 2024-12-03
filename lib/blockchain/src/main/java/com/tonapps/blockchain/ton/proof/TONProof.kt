package com.tonapps.blockchain.ton.proof

import android.os.Parcel
import android.os.Parcelable
import com.tonapps.base64.encodeBase64
import com.tonapps.extensions.toByteArray
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.block.AddrStd
import org.ton.crypto.digest.sha256
import org.ton.crypto.hex

object TONProof {

    private const val tonProofPrefix = "ton-proof-item-v2/"
    private const val tonConnectPrefix = "ton-connect"
    private val prefixMessage = hex("ffff") + tonConnectPrefix.toByteArray()
    private val prefixItem = tonProofPrefix.toByteArray()

    fun sign(
        address: AddrStd,
        secretKey: PrivateKeyEd25519,
        payload: String,
        domain: String,
    ): Result {
        val request = Request(
            payload = payload,
            domain = Domain(domain),
            address = Address(address)
        )

        val body = sha256(prefixMessage + request.signatureMessage)
        val signature = secretKey.sign(body)

        return Result(
            timestamp = request.timestamp,
            domain = request.domain,
            payload = request.payload,
            signature = signature.encodeBase64()
        )
    }

    private interface Serializable {
        fun toByteArray(): ByteArray
    }

    data class Request(
        val timestamp: Long = System.currentTimeMillis() / 1000L,
        val payload: String,
        val domain: Domain,
        val address: Address
    ) {

        val message by lazy {
            prefixItem + address.toByteArray() + domain.toByteArray() + timestamp.toByteArray() + payload.toByteArray()
        }

        val signatureMessage by lazy {
            sha256(message)
        }
    }

    data class Address(val value: AddrStd): Serializable {

        override fun toByteArray(): ByteArray {
            val addressWorkchainBuffer = value.workchainId.toByteArray()
            val addressHashBuffer = value.address.toByteArray()
            return addressWorkchainBuffer + addressHashBuffer
        }
    }

    data class Domain(val value: String): Serializable {

        val lengthBytes: Int = value.toByteArray().size

        override fun toByteArray(): ByteArray {
            return lengthBytes.toByteArray() + value.toByteArray()
        }
    }

    data class Result(
        val timestamp: Long,
        val domain: Domain,
        val payload: String?,
        val signature: String
    ): Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readLong(),
            Domain(parcel.readString()!!),
            parcel.readString(),
            parcel.readString()!!
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeLong(timestamp)
            parcel.writeString(domain.value)
            parcel.writeString(payload)
            parcel.writeString(signature)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Result> {
            override fun createFromParcel(parcel: Parcel): Result {
                return Result(parcel)
            }

            override fun newArray(size: Int): Array<Result?> {
                return arrayOfNulls(size)
            }
        }

    }
}