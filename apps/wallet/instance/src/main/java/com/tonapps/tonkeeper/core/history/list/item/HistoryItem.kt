package com.tonapps.tonkeeper.core.history.list.item

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.tonapps.extensions.readBooleanCompat
import com.tonapps.extensions.readCharSequenceCompat
import com.tonapps.extensions.readEnum
import com.tonapps.extensions.readParcelableCompat
import com.tonapps.extensions.writeBooleanCompat
import com.tonapps.extensions.writeCharSequenceCompat
import com.tonapps.extensions.writeEnum
import com.tonapps.tonkeeper.core.history.ActionType
import com.tonapps.tonkeeper.helper.DateFormat
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.collectibles.entities.NftEntity

sealed class HistoryItem(
    type: Int,
): BaseListItem(type), Parcelable {

    companion object {
        const val TYPE_ACTION = 1
        const val TYPE_HEADER = 2
        const val TYPE_LOADER = 3
        const val TYPE_APP = 4

        fun createFromParcel(parcel: Parcel): HistoryItem {
            return when (parcel.readInt()) {
                TYPE_ACTION -> Event(parcel)
                TYPE_HEADER -> Header(parcel)
                TYPE_LOADER -> Loader(parcel)
                TYPE_APP -> App(parcel)
                else -> throw IllegalArgumentException("Unknown type")
            }
        }
    }

    val timestampForSort: Long by lazy {
        when (this) {
            is Event -> this.timestamp - this.index
            is Loader -> this.date
            is App -> this.timestamp
            else -> 0L
        }
    }

    val uniqueId: String by lazy {
        when (this) {
            is Event -> "event_${this.txId}_${this.index}"
            is Loader -> "loader_${this.index}"
            is App -> "app_${this.timestamp}"
            else -> ""
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(type)
        marshall(dest, flags)
    }

    abstract fun marshall(dest: Parcel, flags: Int)

    override fun describeContents(): Int {
        return 0
    }

    data class Loader(
        val index: Int,
        val date: Long
    ): HistoryItem(TYPE_LOADER) {

        constructor(parcel: Parcel) : this(
            index = parcel.readInt(),
            date = parcel.readLong()
        )

        override fun marshall(dest: Parcel, flags: Int) {
            dest.writeInt(index)
            dest.writeLong(date)
        }

        companion object CREATOR : Parcelable.Creator<Loader> {
            override fun createFromParcel(parcel: Parcel) = Loader(parcel)

            override fun newArray(size: Int): Array<Loader?> {
                return arrayOfNulls(size)
            }
        }
    }

    data class Header(
        val title: String,
        val date: Long,
    ): HistoryItem(TYPE_HEADER) {

        constructor(timestamp: Long) : this(
            title = DateFormat.monthWithDate(timestamp),
            date = timestamp
        )

        constructor(parcel: Parcel) : this(parcel.readLong())

        override fun marshall(dest: Parcel, flags: Int) {
            dest.writeLong(date)
        }

        companion object CREATOR : Parcelable.Creator<Header> {
            override fun createFromParcel(parcel: Parcel) = Header(parcel)

            override fun newArray(size: Int): Array<Header?> {
                return arrayOfNulls(size)
            }
        }
    }

    data class App(
        val iconUri: Uri,
        val title: String,
        val body: String,
        val date: String,
        val host: String,
        val timestamp: Long,
        val deepLink: String
    ): HistoryItem(TYPE_APP) {

        constructor(parcel: Parcel) : this(
            iconUri = parcel.readParcelableCompat()!!,
            title = parcel.readString()!!,
            body = parcel.readString()!!,
            date = parcel.readString()!!,
            host = parcel.readString()!!,
            timestamp = parcel.readLong(),
            deepLink = parcel.readString()!!
        )

        override fun marshall(dest: Parcel, flags: Int) {
            dest.writeParcelable(iconUri, flags)
            dest.writeString(title)
            dest.writeString(body)
            dest.writeString(date)
            dest.writeString(host)
            dest.writeLong(timestamp)
            dest.writeString(deepLink)
        }

        companion object CREATOR : Parcelable.Creator<App> {
            override fun createFromParcel(parcel: Parcel) = App(parcel)

            override fun newArray(size: Int): Array<App?> {
                return arrayOfNulls(size)
            }
        }
    }

    data class Event(
        val index: Int,
        val txId: String,
        val iconURL: String? = null,
        val action: ActionType,
        val title: String,
        val subtitle: String,
        val timestamp: Long = 0L,
        val comment: String? = null,
        val value: CharSequence,
        val value2: CharSequence = "",
        val currency: CharSequence? = null,
        val nft: NftEntity? = null,
        val tokenCode: String? = null,
        val date: String = "",
        val pending: Boolean = false,
        val position: ListCell.Position = ListCell.Position.SINGLE,
        val coinIconUrl: String = "",
        val fee: CharSequence? = null,
        val feeInCurrency: CharSequence? = null,
        val isOut: Boolean,
        val address: String? = null,
        val addressName: String? = null,
        val lt: Long = 0L,
        val failed: Boolean,
        val cipherText: String? = null,
        val hiddenBalance: Boolean = false
    ): HistoryItem(TYPE_ACTION) {

        val hasNft: Boolean
            get() = nft != null

        constructor(parcel: Parcel) : this(
            index = parcel.readInt(),
            txId = parcel.readString()!!,
            iconURL = parcel.readString(),
            action = parcel.readEnum(ActionType::class.java)!!,
            title = parcel.readString()!!,
            subtitle = parcel.readString()!!,
            timestamp = parcel.readLong(),
            comment = parcel.readString(),
            value = parcel.readCharSequenceCompat()!!,
            value2 = parcel.readCharSequenceCompat()!!,
            currency = parcel.readCharSequenceCompat(),
            nft = parcel.readParcelableCompat(),
            tokenCode = parcel.readString(),
            date = parcel.readString()!!,
            pending = parcel.readBooleanCompat(),
            position = parcel.readEnum(ListCell.Position::class.java)!!,
            coinIconUrl = parcel.readString()!!,
            fee = parcel.readCharSequenceCompat(),
            feeInCurrency = parcel.readCharSequenceCompat(),
            isOut = parcel.readBooleanCompat(),
            address = parcel.readString(),
            addressName = parcel.readString(),
            lt = parcel.readLong(),
            failed = parcel.readBooleanCompat(),
            cipherText = parcel.readString(),
            hiddenBalance = parcel.readBooleanCompat()
        )

        override fun marshall(dest: Parcel, flags: Int) {
            dest.writeInt(index)
            dest.writeString(txId)
            dest.writeString(iconURL)
            dest.writeEnum(action)
            dest.writeString(title)
            dest.writeString(subtitle)
            dest.writeLong(timestamp)
            dest.writeString(comment)
            dest.writeCharSequenceCompat(value)
            dest.writeCharSequenceCompat(value2)
            dest.writeCharSequenceCompat(currency)
            dest.writeParcelable(nft, flags)
            dest.writeString(tokenCode)
            dest.writeString(date)
            dest.writeBooleanCompat(pending)
            dest.writeEnum(position)
            dest.writeString(coinIconUrl)
            dest.writeCharSequenceCompat(fee)
            dest.writeCharSequenceCompat(feeInCurrency)
            dest.writeBooleanCompat(isOut)
            dest.writeString(address)
            dest.writeString(addressName)
            dest.writeLong(lt)
            dest.writeBooleanCompat(failed)
            dest.writeString(cipherText)
            dest.writeBooleanCompat(hiddenBalance)
        }

        companion object CREATOR : Parcelable.Creator<Event> {
            override fun createFromParcel(parcel: Parcel) = Event(parcel)

            override fun newArray(size: Int): Array<Event?> {
                return arrayOfNulls(size)
            }
        }
    }
}