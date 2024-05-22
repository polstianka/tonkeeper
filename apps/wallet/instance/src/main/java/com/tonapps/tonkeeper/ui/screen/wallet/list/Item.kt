package com.tonapps.tonkeeper.ui.screen.wallet.list

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.tonapps.extensions.readArrayCompat
import com.tonapps.extensions.readBooleanCompat
import com.tonapps.extensions.readCharSequenceCompat
import com.tonapps.extensions.readEnum
import com.tonapps.extensions.readParcelableCompat
import com.tonapps.extensions.writeArrayCompat
import com.tonapps.extensions.writeBooleanCompat
import com.tonapps.extensions.writeCharSequenceCompat
import com.tonapps.extensions.writeEnum
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakedBalance
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.WalletType
import com.tonapps.wallet.data.push.entities.AppPushEntity
import com.tonapps.wallet.data.tonconnect.entities.DAppEntity
import java.math.BigDecimal

sealed class Item(type: Int): BaseListItem(type), Parcelable {

    companion object {
        const val TYPE_BALANCE = 0
        const val TYPE_ACTIONS = 1
        const val TYPE_TOKEN = 2
        const val TYPE_SPACE = 3
        const val TYPE_SKELETON = 4
        const val TYPE_PUSH = 5
        const val TYPE_STAKED = 6

        fun createFromParcel(parcel: Parcel): Item {
            return when (parcel.readInt()) {
                TYPE_BALANCE -> Balance(parcel)
                TYPE_ACTIONS -> Actions(parcel)
                TYPE_TOKEN -> Token(parcel)
                TYPE_SPACE -> Space(parcel)
                TYPE_SKELETON -> Skeleton(parcel)
                TYPE_PUSH -> Push(parcel)
                TYPE_STAKED -> StakedItem(parcel)
                else -> throw IllegalArgumentException("Unknown type")
            }
        }
    }

    enum class Status {
        Default,
        Updating,
        NoInternet,
        SendingTransaction,
        TransactionConfirmed,
        Unknown,
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(type)
        marshall(dest, flags)
    }

    abstract fun marshall(dest: Parcel, flags: Int)

    override fun describeContents(): Int {
        return 0
    }

    data class Balance(
        val balance: CharSequence,
        val address: String,
        val walletType: WalletType,
        val status: Status,
        val hiddenBalance: Boolean
    ): Item(TYPE_BALANCE) {

        constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readEnum(WalletType::class.java)!!,
            parcel.readEnum(Status::class.java)!!,
            parcel.readBooleanCompat()
        )

        override fun marshall(dest: Parcel, flags: Int) {
            dest.writeString(balance.toString())
            dest.writeString(address)
            dest.writeEnum(walletType)
            dest.writeEnum(status)
            dest.writeBooleanCompat(hiddenBalance)
        }

        companion object CREATOR : Parcelable.Creator<Balance> {
            override fun createFromParcel(parcel: Parcel) = Balance(parcel)

            override fun newArray(size: Int): Array<Balance?> {
                return arrayOfNulls(size)
            }
        }
    }

    data class Actions(
        val address: String,
        val token: TokenEntity,
        val walletType: WalletType,
        val swapUri: Uri,
        val disableSwap: Boolean
    ): Item(TYPE_ACTIONS) {

        constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readParcelableCompat()!!,
            parcel.readEnum(WalletType::class.java)!!,
            parcel.readParcelableCompat()!!,
            parcel.readBooleanCompat()
        )

        override fun marshall(dest: Parcel, flags: Int) {
            dest.writeString(address)
            dest.writeParcelable(token, flags)
            dest.writeEnum(walletType)
            dest.writeParcelable(swapUri, flags)
            dest.writeBooleanCompat(disableSwap)
        }

        companion object CREATOR : Parcelable.Creator<Actions> {
            override fun createFromParcel(parcel: Parcel) = Actions(parcel)

            override fun newArray(size: Int): Array<Actions?> {
                return arrayOfNulls(size)
            }
        }
    }

    data class Token(
        val position: ListCell.Position,
        val iconUri: Uri,
        val address: String,
        val symbol: String,
        val name: String,
        val balance: BigDecimal,
        val balanceFormat: CharSequence,
        val fiat: BigDecimal,
        val fiatFormat: CharSequence,
        val rate: CharSequence,
        val rateDiff24h: String,
        val verified: Boolean,
        val testnet: Boolean,
        val hiddenBalance: Boolean
    ): Item(TYPE_TOKEN) {

        constructor(parcel: Parcel) : this(
            parcel.readEnum(ListCell.Position::class.java)!!,
            parcel.readParcelableCompat()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readSerializable()!! as BigDecimal,
            parcel.readCharSequenceCompat()!!,
            parcel.readSerializable()!! as BigDecimal,
            parcel.readCharSequenceCompat()!!,
            parcel.readCharSequenceCompat()!!,
            parcel.readString()!!,
            parcel.readBooleanCompat(),
            parcel.readBooleanCompat(),
            parcel.readBooleanCompat()
        )

        override fun marshall(dest: Parcel, flags: Int) {
            dest.writeEnum(position)
            dest.writeParcelable(iconUri, flags)
            dest.writeString(address)
            dest.writeString(symbol)
            dest.writeString(name)
            dest.writeSerializable(balance)
            dest.writeCharSequenceCompat(balanceFormat)
            dest.writeSerializable(fiat)
            dest.writeCharSequenceCompat(fiatFormat)
            dest.writeCharSequenceCompat(rate)
            dest.writeString(rateDiff24h)
            dest.writeBooleanCompat(verified)
            dest.writeBooleanCompat(testnet)
            dest.writeBooleanCompat(hiddenBalance)
        }

        companion object CREATOR : Parcelable.Creator<Token> {
            override fun createFromParcel(parcel: Parcel) = Token(parcel)

            override fun newArray(size: Int): Array<Token?> {
                return arrayOfNulls(size)
            }
        }
    }

    data class StakedItem(
        val position: ListCell.Position,
        val balance: StakedBalance,
        val balanceFiat: BigDecimal
    ) : Item(TYPE_STAKED) {

        companion object CREATOR : Parcelable.Creator<StakedItem> {
            override fun createFromParcel(source: Parcel) = StakedItem(source)

            override fun newArray(size: Int): Array<StakedItem?> {
                return arrayOfNulls(size)
            }
        }
        override fun marshall(dest: Parcel, flags: Int) {
            dest.writeEnum(position)
            dest.writeParcelable(balance, flags)
            dest.writeSerializable(balanceFiat)
        }

        constructor(parcel: Parcel) : this(
            position = parcel.readEnum(ListCell.Position::class.java)!!,
            balance = parcel.readParcelableCompat()!!,
            balanceFiat = parcel.readSerializable() as BigDecimal
        )
    }

    data class Space(val value: Boolean = true): Item(TYPE_SPACE) {

        constructor(parcel: Parcel) : this(
            parcel.readBooleanCompat()
        )

        override fun marshall(dest: Parcel, flags: Int) {
            dest.writeBooleanCompat(value)
        }

        companion object CREATOR : Parcelable.Creator<Space> {
            override fun createFromParcel(parcel: Parcel) = Space(parcel)

            override fun newArray(size: Int): Array<Space?> {
                return arrayOfNulls(size)
            }
        }
    }

    data class Skeleton(val value: Boolean = true): Item(TYPE_SKELETON) {

        constructor(parcel: Parcel) : this(
            parcel.readBooleanCompat()
        )

        override fun marshall(dest: Parcel, flags: Int) {
            dest.writeBooleanCompat(value)
        }

        companion object CREATOR : Parcelable.Creator<Skeleton> {
            override fun createFromParcel(parcel: Parcel) = Skeleton(parcel)

            override fun newArray(size: Int): Array<Skeleton?> {
                return arrayOfNulls(size)
            }
        }
    }

    data class Push(
        val events: List<AppPushEntity>,
        val apps: List<DAppEntity>
    ): Item(TYPE_PUSH) {

        constructor(parcel: Parcel) : this(
            parcel.readArrayCompat(AppPushEntity::class.java)?.toList()!!,
            parcel.readArrayCompat(DAppEntity::class.java)?.toList()!!,
        )

        val text = events.first().message

        val iconUris: List<Uri> by lazy {
            apps.map { Uri.parse(it.manifest.iconUrl) }
        }

        override fun marshall(dest: Parcel, flags: Int) {
            dest.writeArrayCompat(events.toTypedArray())
            dest.writeArrayCompat(apps.toTypedArray())
        }

        companion object CREATOR : Parcelable.Creator<Push> {
            override fun createFromParcel(parcel: Parcel) = Push(parcel)

            override fun newArray(size: Int): Array<Push?> {
                return arrayOfNulls(size)
            }
        }
    }
}