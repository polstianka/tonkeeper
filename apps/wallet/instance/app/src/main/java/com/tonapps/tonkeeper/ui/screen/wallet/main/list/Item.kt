package com.tonapps.tonkeeper.ui.screen.wallet.main.list

import android.content.Context
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.facebook.common.util.UriUtil
import com.tonapps.blockchain.ton.contract.WalletVersion
import com.tonapps.icu.Coins
import com.tonapps.extensions.readArrayCompat
import com.tonapps.extensions.readBooleanCompat
import com.tonapps.extensions.readCharSequenceCompat
import com.tonapps.extensions.readEnum
import com.tonapps.extensions.readParcelableCompat
import com.tonapps.extensions.writeArrayCompat
import com.tonapps.extensions.writeBooleanCompat
import com.tonapps.extensions.writeCharSequenceCompat
import com.tonapps.extensions.writeEnum
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.view.BatteryView
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.entity.NotificationEntity
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.Wallet
import com.tonapps.wallet.data.push.entities.AppPushEntity
import com.tonapps.wallet.data.staking.StakingPool
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import com.tonapps.wallet.data.tonconnect.entities.DAppManifestEntity
import com.tonapps.wallet.data.tonconnect.entities.DConnectEntity

sealed class Item(type: Int): BaseListItem(type), Parcelable {

    companion object {
        const val TYPE_BALANCE = 0
        const val TYPE_ACTIONS = 1
        const val TYPE_TOKEN = 2
        const val TYPE_SPACE = 3
        const val TYPE_SKELETON = 4
        const val TYPE_PUSH = 5
        const val TYPE_TITLE = 6
        const val TYPE_MANAGE = 7
        const val TYPE_ALERT = 8
        const val TYPE_SETUP_TITLE = 9
        const val TYPE_SETUP_SWITCH = 10
        const val TYPE_SETUP_LINK = 11
        const val TYPE_STAKED = 12

        fun createFromParcel(parcel: Parcel): Item {
            return when (parcel.readInt()) {
                TYPE_BALANCE -> Balance(parcel)
                TYPE_ACTIONS -> Actions(parcel)
                TYPE_TOKEN -> Token(parcel)
                TYPE_SPACE -> Space(parcel)
                TYPE_SKELETON -> Skeleton(parcel)
                TYPE_PUSH -> Push(parcel)
                TYPE_MANAGE -> Manage(parcel)
                TYPE_SETUP_TITLE -> SetupTitle(parcel)
                TYPE_SETUP_SWITCH -> SetupSwitch(parcel)
                TYPE_SETUP_LINK -> SetupLink(parcel)
                TYPE_STAKED -> Stake(parcel)
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
        LastUpdated,
    }

    enum class BalanceType {
        Zero,
        Positive,
        Huge,
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(type)
        marshall(dest, flags)
    }

    abstract fun marshall(dest: Parcel, flags: Int)

    override fun describeContents(): Int {
        return 0
    }

    data class Alert(
        val title: String,
        val message: String,
        val buttonTitle: String?,
        val buttonUrl: String?,
    ): Item(TYPE_ALERT) {

        constructor(entity: NotificationEntity) : this(
            title = entity.title,
            message = entity.caption,
            buttonTitle = entity.action?.label,
            buttonUrl = entity.action?.url
        )

        constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString(),
            parcel.readString()
        )

        override fun marshall(dest: Parcel, flags: Int) {
            dest.writeString(title)
            dest.writeString(message)
            dest.writeString(buttonTitle)
            dest.writeString(buttonUrl)
        }

        companion object CREATOR : Parcelable.Creator<Alert> {
            override fun createFromParcel(parcel: Parcel) = Alert(parcel)

            override fun newArray(size: Int): Array<Alert?> = arrayOfNulls(size)
        }

    }

    data class Balance(
        val balance: CharSequence,
        val address: String,
        val walletType: Wallet.Type,
        val walletVersion: WalletVersion,
        val status: Status,
        val hiddenBalance: Boolean,
        val hasBackup: Boolean,
        val balanceType: BalanceType,
        val lastUpdatedFormat: String,
        val batteryBalance: Coins,
        val showBattery: Boolean,
        val batteryEmptyState: BatteryView.EmptyState,
    ): Item(TYPE_BALANCE) {

        constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readEnum(Wallet.Type::class.java)!!,
            parcel.readEnum(WalletVersion::class.java)!!,
            parcel.readEnum(Status::class.java)!!,
            parcel.readBooleanCompat(),
            parcel.readBooleanCompat(),
            parcel.readEnum(BalanceType::class.java)!!,
            parcel.readString()!!,
            parcel.readParcelableCompat()!!,
            parcel.readBooleanCompat(),
            parcel.readEnum(BatteryView.EmptyState::class.java)!!,
        )

        override fun marshall(dest: Parcel, flags: Int) {
            dest.writeString(balance.toString())
            dest.writeString(address)
            dest.writeEnum(walletType)
            dest.writeEnum(walletVersion)
            dest.writeEnum(status)
            dest.writeBooleanCompat(hiddenBalance)
            dest.writeBooleanCompat(hasBackup)
            dest.writeEnum(balanceType)
            dest.writeString(lastUpdatedFormat)
            dest.writeParcelable(batteryBalance, flags)
            dest.writeBooleanCompat(showBattery)
            dest.writeEnum(batteryEmptyState)
        }

        companion object CREATOR : Parcelable.Creator<Balance> {
            override fun createFromParcel(parcel: Parcel) = Balance(parcel)

            override fun newArray(size: Int): Array<Balance?> = arrayOfNulls(size)
        }
    }

    data class Actions(
        val address: String,
        val token: TokenEntity,
        val walletType: Wallet.Type,
        val swapUri: Uri,
        val disableSwap: Boolean
    ): Item(TYPE_ACTIONS) {

        constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readParcelableCompat()!!,
            parcel.readEnum(Wallet.Type::class.java)!!,
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

            override fun newArray(size: Int): Array<Actions?> = arrayOfNulls(size)
        }
    }

    data class Stake(
        val position: ListCell.Position,
        val poolAddress: String,
        val poolName: String,
        val poolImplementation: StakingPool.Implementation,
        val balance: Coins,
        val balanceFormat: CharSequence,
        val message: String?,
        val fiat: Coins,
        val fiatFormat: CharSequence,
    ): Item(TYPE_STAKED) {

        val iconUri: Uri
            get() = UriUtil.getUriForResourceId(StakingPool.getIcon(poolImplementation))

        constructor(parcel: Parcel) : this(
            parcel.readEnum(ListCell.Position::class.java)!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readEnum(StakingPool.Implementation::class.java)!!,
            parcel.readParcelableCompat()!!,
            parcel.readCharSequenceCompat()!!,
            parcel.readString(),
            parcel.readParcelableCompat()!!,
            parcel.readCharSequenceCompat()!!
        )

        override fun marshall(dest: Parcel, flags: Int) {
            dest.writeEnum(position)
            dest.writeString(poolAddress)
            dest.writeString(poolName)
            dest.writeEnum(poolImplementation)
            dest.writeParcelable(balance, flags)
            dest.writeCharSequenceCompat(balanceFormat)
            dest.writeString(message)
            dest.writeParcelable(fiat, flags)
            dest.writeCharSequenceCompat(fiatFormat)
        }

        companion object CREATOR : Parcelable.Creator<Stake> {
            override fun createFromParcel(parcel: Parcel) = Stake(parcel)

            override fun newArray(size: Int): Array<Stake?> = arrayOfNulls(size)
        }
    }

    data class Token(
        val position: ListCell.Position,
        val iconUri: Uri,
        val address: String,
        val symbol: String,
        val name: String,
        val balance: Coins,
        val balanceFormat: CharSequence,
        val fiat: Coins,
        val fiatFormat: CharSequence,
        val rate: CharSequence,
        val rateDiff24h: String,
        val verified: Boolean,
        val testnet: Boolean,
        val hiddenBalance: Boolean,
        val blacklist: Boolean
    ): Item(TYPE_TOKEN) {

        val isTON = symbol == TokenEntity.TON.symbol

        val isUSDT = symbol == TokenEntity.USDT.symbol

        constructor(
            position: ListCell.Position,
            token: AccountTokenEntity,
            hiddenBalance: Boolean,
            testnet: Boolean,
            currencyCode: String
        ) : this(
            position = position,
            iconUri = token.imageUri,
            address = token.address,
            symbol = token.symbol,
            name = token.name,
            balance = token.balance.value,
            balanceFormat = CurrencyFormatter.format(value = token.balance.value),
            fiat = token.fiat,
            fiatFormat = if (testnet) "" else CurrencyFormatter.formatFiat(currencyCode, token.fiat),
            rate = CurrencyFormatter.formatFiat(currencyCode, token.rateNow),
            rateDiff24h = token.rateDiff24h,
            verified = token.verified,
            testnet = testnet,
            hiddenBalance = hiddenBalance,
            blacklist = token.blacklist
        )

        constructor(parcel: Parcel) : this(
            parcel.readEnum(ListCell.Position::class.java)!!,
            parcel.readParcelableCompat()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readParcelableCompat()!!,
            parcel.readCharSequenceCompat() ?: "",
            parcel.readParcelableCompat()!!,
            parcel.readCharSequenceCompat()!!,
            parcel.readCharSequenceCompat()!!,
            parcel.readString()!!,
            parcel.readBooleanCompat(),
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
            dest.writeParcelable(balance, flags)
            dest.writeCharSequenceCompat(balanceFormat)
            dest.writeParcelable(fiat, flags)
            dest.writeCharSequenceCompat(fiatFormat)
            dest.writeCharSequenceCompat(rate)
            dest.writeString(rateDiff24h)
            dest.writeBooleanCompat(verified)
            dest.writeBooleanCompat(testnet)
            dest.writeBooleanCompat(hiddenBalance)
            dest.writeBooleanCompat(blacklist)
        }

        companion object CREATOR : Parcelable.Creator<Token> {
            override fun createFromParcel(parcel: Parcel) = Token(parcel)

            override fun newArray(size: Int): Array<Token?> = arrayOfNulls(size)
        }
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

            override fun newArray(size: Int): Array<Space?> = arrayOfNulls(size)
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

            override fun newArray(size: Int): Array<Skeleton?> = arrayOfNulls(size)
        }
    }

    data class Push(
        val events: List<AppPushEntity>,
        val apps: List<DConnectEntity>,
        val manifests: List<DAppManifestEntity>
    ): Item(TYPE_PUSH) {

        constructor(parcel: Parcel) : this(
            parcel.readArrayCompat(AppPushEntity::class.java)?.toList()!!,
            parcel.readArrayCompat(DConnectEntity::class.java)?.toList()!!,
            parcel.readArrayCompat(DAppManifestEntity::class.java)?.toList()!!
        )

        val text = events.first().message

        val iconUris: List<Uri> by lazy {
            manifests.map { Uri.parse(it.iconUrl) }
        }

        override fun marshall(dest: Parcel, flags: Int) {
            dest.writeArrayCompat(events.toTypedArray())
            dest.writeArrayCompat(apps.toTypedArray())
        }

        companion object CREATOR : Parcelable.Creator<Push> {
            override fun createFromParcel(parcel: Parcel) = Push(parcel)

            override fun newArray(size: Int): Array<Push?> = arrayOfNulls(size)
        }
    }

    data class Title(
        val title: CharSequence
    ): Item(TYPE_TITLE) {

        constructor(context: Context, resId: Int) : this(
            context.getText(resId)
        )

        constructor(parcel: Parcel) : this(
            parcel.readCharSequenceCompat()!!
        )

        override fun marshall(dest: Parcel, flags: Int) {
            dest.writeCharSequenceCompat(title)
        }

        companion object CREATOR : Parcelable.Creator<Title> {
            override fun createFromParcel(parcel: Parcel) = Title(parcel)

            override fun newArray(size: Int): Array<Title?> = arrayOfNulls(size)
        }
    }

    data class Manage(val value: Boolean): Item(TYPE_MANAGE) {

        constructor(parcel: Parcel) : this(
            parcel.readBooleanCompat()
        )

        override fun marshall(dest: Parcel, flags: Int) {
            dest.writeBooleanCompat(value)
        }

        companion object CREATOR : Parcelable.Creator<Manage> {
            override fun createFromParcel(parcel: Parcel) = Manage(parcel)

            override fun newArray(size: Int): Array<Manage?> = arrayOfNulls(size)
        }
    }

    data class SetupTitle(
        val walletId: String,
        val showDone: Boolean
    ): Item(TYPE_SETUP_TITLE) {

        constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readBooleanCompat()
        )

        override fun marshall(dest: Parcel, flags: Int) {
            dest.writeString(walletId)
            dest.writeBooleanCompat(showDone)
        }

        companion object CREATOR : Parcelable.Creator<SetupTitle> {
            override fun createFromParcel(parcel: Parcel) = SetupTitle(parcel)

            override fun newArray(size: Int): Array<SetupTitle?> = arrayOfNulls(size)
        }
    }

    data class SetupSwitch(
        val position: ListCell.Position,
        val iconRes: Int,
        val textRes: Int,
        val enabled: Boolean,
        val walletId: String,
        val settingsType: Int
    ): Item(TYPE_SETUP_SWITCH) {

        companion object {
            const val TYPE_BIOMETRIC = 1
            const val TYPE_PUSH = 2

            @JvmField
            val CREATOR = object : Parcelable.Creator<SetupSwitch> {
                override fun createFromParcel(parcel: Parcel) = SetupSwitch(parcel)

                override fun newArray(size: Int): Array<SetupSwitch?> = arrayOfNulls(size)
            }
        }

        constructor(parcel: Parcel) : this(
            parcel.readEnum(ListCell.Position::class.java)!!,
            parcel.readInt(),
            parcel.readInt(),
            parcel.readBooleanCompat(),
            parcel.readString()!!,
            parcel.readInt()
        )

        override fun marshall(dest: Parcel, flags: Int) {
            dest.writeEnum(position)
            dest.writeInt(iconRes)
            dest.writeInt(textRes)
            dest.writeBooleanCompat(enabled)
            dest.writeString(walletId)
            dest.writeInt(settingsType)
        }
    }

    data class SetupLink(
        val position: ListCell.Position,
        val walletId: String,
        val iconRes: Int,
        val textRes: Int,
        val link: String,
        val blue: Boolean,
        val settingsType: Int
    ): Item(TYPE_SETUP_LINK) {

        companion object {
            const val TYPE_NONE = 1
            const val TYPE_TELEGRAM_CHANNEL = 2

            @JvmField
            val CREATOR = object : Parcelable.Creator<SetupLink> {
                override fun createFromParcel(parcel: Parcel) = SetupLink(parcel)

                override fun newArray(size: Int): Array<SetupLink?> = arrayOfNulls(size)
            }
        }

        constructor(parcel: Parcel) : this(
            parcel.readEnum(ListCell.Position::class.java)!!,
            parcel.readString()!!,
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString()!!,
            parcel.readBooleanCompat(),
            parcel.readInt(),
        )

        override fun marshall(dest: Parcel, flags: Int) {
            dest.writeEnum(position)
            dest.writeString(walletId)
            dest.writeInt(iconRes)
            dest.writeInt(textRes)
            dest.writeString(link)
            dest.writeBooleanCompat(blue)
            dest.writeInt(settingsType)
        }
    }
}