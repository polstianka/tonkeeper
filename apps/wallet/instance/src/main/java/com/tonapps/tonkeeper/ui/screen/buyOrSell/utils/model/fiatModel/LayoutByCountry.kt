package com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.fiatModel

import android.os.Parcel
import android.os.Parcelable
import com.tonapps.tonkeeper.ui.screen.swap.model.assets.Asset
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LayoutByCountry(
    @SerialName("countryCode")val countryCode: String,
    @SerialName("currency")val currency: String,
    @SerialName("methods")val methods: List<String>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        countryCode = parcel.readString() ?: "",
        currency = parcel.readString() ?: "",
        methods = parcel.createStringArrayList() ?: listOf(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(countryCode)
        parcel.writeString(currency)
        parcel.writeList(methods)
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