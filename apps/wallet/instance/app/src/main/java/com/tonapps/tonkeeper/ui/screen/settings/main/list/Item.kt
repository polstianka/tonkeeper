package com.tonapps.tonkeeper.ui.screen.settings.main.list

import com.tonapps.blockchain.ton.contract.WalletVersion
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.icon.UIKitIcon
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.account.Wallet
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.localization.Localization

sealed class Item(type: Int): BaseListItem(type) {

    companion object {
        const val TYPE_ACCOUNT = 0
        const val TYPE_SPACE = 1
        const val TYPE_TEXT = 2
        const val TYPE_ICON = 3
        const val TYPE_LOGO = 4
    }

    data class Account(
        val wallet: WalletEntity
    ): Item(TYPE_ACCOUNT) {

        val title: String
            get() = wallet.label.name

        val emoji: String
            get() = wallet.label.emoji.toString()

        val color: Int
            get() = wallet.label.color

        val walletType: Wallet.Type
            get() = wallet.type

        val walletVersion: WalletVersion
            get() = wallet.version

    }

    data object Space: Item(TYPE_SPACE)

    data object Logo: Item(TYPE_LOGO)

    sealed class Text(
        val titleRes: Int,
        val value: String,
        open val position: ListCell.Position
    ): Item(TYPE_TEXT)

    class Currency(
        code: String,
        position: ListCell.Position
    ): Text(
        titleRes = Localization.currency,
        value = code,
        position = position
    )

    data class ConnectedApps(
        override val position: ListCell.Position
    ): Icon(
        titleRes = Localization.connected_apps,
        iconRes = UIKitIcon.ic_apps_28,
        position = position,
        secondaryIcon = false
    )

    sealed class Icon(
        val titleRes: Int,
        val iconRes: Int,
        open val position: ListCell.Position,
        val secondaryIcon: Boolean,
        val dot: Boolean = false,
    ): Item(TYPE_ICON)

    data class Theme(
        override val position: ListCell.Position
    ): Icon(
        titleRes = Localization.appearance,
        iconRes = UIKitIcon.ic_appearance_28,
        position = position,
        secondaryIcon = false
    )

    data class Widget(
        override val position: ListCell.Position
    ): Icon(
        titleRes = Localization.widget,
        iconRes = com.tonapps.uikit.icon.R.drawable.ic_link_square_28,
        position = position,
        secondaryIcon = false
    )

    data class Backup(
        override val position: ListCell.Position,
        val hasBackup: Boolean,
    ): Icon(
        titleRes = Localization.backup,
        iconRes = UIKitIcon.ic_key_28,
        position = position,
        secondaryIcon = false,
        dot = !hasBackup
    )

    data class Support(
        override val position: ListCell.Position,
        val url: String
    ): Icon(
        titleRes = Localization.support,
        iconRes = UIKitIcon.ic_message_bubble_28,
        position = position,
        secondaryIcon = false
    )

    data class FAQ(
        override val position: ListCell.Position,
        val url: String
    ): Icon(
        titleRes = Localization.faq,
        iconRes = UIKitIcon.ic_question_28,
        position = position,
        secondaryIcon = false
    )

    data class Tester(
        override val position: ListCell.Position,
        val url: String
    ): Icon(
        titleRes = Localization.testers_chat,
        iconRes = R.drawable.ic_telegram_28,
        position = position,
        secondaryIcon = false,
        dot = true
    )

    data class News(
        override val position: ListCell.Position,
        val url: String
    ): Icon(
        titleRes = Localization.tonkeeper_news,
        iconRes = R.drawable.ic_telegram_28,
        position = position,
        secondaryIcon = true
    )

    data class Contact(
        override val position: ListCell.Position,
        val url: String
    ): Icon(
        titleRes = Localization.contact_us,
        iconRes = R.drawable.ic_envelope_28,
        position = position,
        secondaryIcon = true
    )

    data class Legal(
        override val position: ListCell.Position
    ): Icon(
        titleRes = Localization.legal,
        iconRes = UIKitIcon.ic_doc_28,
        position = position,
        secondaryIcon = true
    )

    data class Notifications(
        override val position: ListCell.Position
    ): Icon(
        titleRes = Localization.notifications,
        iconRes = UIKitIcon.ic_notifications_28,
        position = position,
        secondaryIcon = false
    )

    data class Logout(
        override val position: ListCell.Position,
        val label: Wallet.Label,
        val delete: Boolean,
    ): Icon(
        titleRes = Localization.log_out,
        iconRes = R.drawable.ic_door_28,
        position = position,
        secondaryIcon = false
    )

    data class Security(
        override val position: ListCell.Position
    ): Icon(
        titleRes = Localization.security,
        iconRes = UIKitIcon.ic_lock_28,
        position = position,
        secondaryIcon = false
    )

    data class DeleteWatchAccount(
        override val position: ListCell.Position
    ): Icon(
        titleRes = Localization.delete_watch_account,
        iconRes = UIKitIcon.ic_trash_bin_28,
        position = position,
        secondaryIcon = false
    )

    data class SearchEngine(
        val engine: com.tonapps.wallet.data.core.SearchEngine,
        override val position: ListCell.Position
    ): Text(
        titleRes = Localization.search,
        value = engine.title,
        position = position
    )

    data class Language(
        val data: String,
        override val position: ListCell.Position
    ): Text(
        titleRes = Localization.language,
        value = data,
        position = position
    )

    data class Rate(
        override val position: ListCell.Position
    ): Icon(
        titleRes = Localization.rate_tonkeeper,
        iconRes = UIKitIcon.ic_star_28,
        position = position,
        secondaryIcon = true
    )

    data class W5(
        override val position: ListCell.Position
    ): Icon(
        titleRes = Localization.w5_wallet,
        iconRes = UIKitIcon.ic_wallet_28,
        position = position,
        secondaryIcon = false
    )

    data class V4R2(
        override val position: ListCell.Position
    ): Icon(
        titleRes = Localization.v4r2_wallet,
        iconRes = UIKitIcon.ic_wallet_28,
        position = position,
        secondaryIcon = false
    )

    data class Battery(
        override val position: ListCell.Position
    ): Icon(
        titleRes = Localization.battery,
        iconRes = UIKitIcon.ic_battery_28,
        position = position,
        secondaryIcon = false
    )

    data class Cards(
        override val position: ListCell.Position
    ): Icon(
        titleRes = Localization.bank_card_title,
        iconRes = UIKitIcon.ic_creditcard_28,
        position = position,
        secondaryIcon = false
    )
}