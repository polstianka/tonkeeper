package com.tonapps.tonkeeper.ui.screen.swap.model.assets

import android.os.Parcel
import android.os.Parcelable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Asset(
    @SerialName("contract_address") val contract_address: String,
    @SerialName("symbol") val symbol: String,
    @SerialName("display_name") val display_name: String,
    @SerialName("image_url") val image_url: String,
    @SerialName("decimals") val decimals: Int,
    @SerialName("kind") val kind: String,
    @SerialName("wallet_address") val wallet_address: String? = null,
    @SerialName("balance") val balance: String? = null,
    @SerialName("third_party_usd_price") val third_party_usd_price: String? = null,
    @SerialName("dex_usd_price") val dex_usd_price: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        contract_address = parcel.readString() ?: "",
        symbol = parcel.readString() ?: "",
        display_name = parcel.readString() ?: "",
        image_url = parcel.readString() ?: "",
        decimals = parcel.readInt(),
        kind = parcel.readString() ?: "",
        wallet_address = parcel.readString(),
        balance = parcel.readString(),
        third_party_usd_price = parcel.readString(),
        dex_usd_price = parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(contract_address)
        parcel.writeString(symbol)
        parcel.writeString(display_name)
        parcel.writeString(image_url)
        parcel.writeInt(decimals)
        parcel.writeString(kind)
        parcel.writeString(wallet_address)
        parcel.writeString(balance)
        parcel.writeString(third_party_usd_price)
        parcel.writeString(dex_usd_price)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Asset> {
        override fun createFromParcel(parcel: Parcel): Asset {
            return Asset(parcel)
        }

        override fun newArray(size: Int): Array<Asset?> {
            return arrayOfNulls(size)
        }
    }
}



