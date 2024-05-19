package com.tonapps.tonkeeper.ui.screen.settings.main.list

import android.net.Uri
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.icon.UIKitIcon
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.account.WalletType
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.settings.SettingsRepository
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
        val title: String,
        val emoji: String,
        val color: Int,
        val walletType: WalletType
    ): Item(TYPE_ACCOUNT) {

        constructor(wallet: WalletEntity) : this(
            title = wallet.label.name,
            emoji = wallet.label.emoji.toString(),
            color = wallet.label.color,
            walletType = wallet.type
        )
    }

    data object Space: Item(TYPE_SPACE)

    data object Logo: Item(TYPE_LOGO)

    open class Text(
        val titleRes: Int,
        val value: String,
        val position: ListCell.Position
    ): Item(TYPE_TEXT)

    class Currency(
        code: String,
        position: ListCell.Position
    ): Text(
        titleRes = Localization.currency,
        value = code,
        position = position
    )

    class Language(
        data: String,
        position: ListCell.Position
    ): Text(
        titleRes = Localization.language,
        value = data,
        position = position
    )

    open class Icon(
        val titleRes: Int,
        val iconRes: Int,
        val position: ListCell.Position,
        val secondaryIcon: Boolean
    ): Item(TYPE_ICON)

    class Theme(
        position: ListCell.Position
    ): Icon(
        titleRes = Localization.theme,
        iconRes = UIKitIcon.ic_appearance_28,
        position = position,
        secondaryIcon = false
    )

    class Widget(
        position: ListCell.Position
    ): Icon(
        titleRes = Localization.widget,
        iconRes = R.drawable.ic_widget_28,
        position = position,
        secondaryIcon = false
    )

    class Support(
        position: ListCell.Position,
        val url: String
    ): Icon(
        titleRes = Localization.support,
        iconRes = UIKitIcon.ic_message_bubble_28,
        position = position,
        secondaryIcon = false
    )

    class News(
        position: ListCell.Position,
        val url: String
    ): Icon(
        titleRes = Localization.tonkeeper_news,
        iconRes = R.drawable.ic_telegram_28,
        position = position,
        secondaryIcon = true
    )

    class Contact(
        position: ListCell.Position,
        val url: String
    ): Icon(
        titleRes = Localization.contact_us,
        iconRes = R.drawable.ic_envelope_28,
        position = position,
        secondaryIcon = true
    )

    class Legal(
        position: ListCell.Position
    ): Icon(
        titleRes = Localization.legal,
        iconRes = UIKitIcon.ic_doc_28,
        position = position,
        secondaryIcon = true
    )

    class Notifications(
        position: ListCell.Position
    ): Icon(
        titleRes = Localization.notifications,
        iconRes = UIKitIcon.ic_notifications_28,
        position = position,
        secondaryIcon = false
    )

    class Logout(
        position: ListCell.Position
    ): Icon(
        titleRes = Localization.log_out,
        iconRes = R.drawable.ic_door_28,
        position = position,
        secondaryIcon = false
    )

    class Security(
        position: ListCell.Position
    ): Icon(
        titleRes = Localization.security,
        iconRes = UIKitIcon.ic_lock_28,
        position = position,
        secondaryIcon = false
    )

    class DeleteWatchAccount(
        position: ListCell.Position
    ): Icon(
        titleRes = Localization.delete_watch_account,
        iconRes = UIKitIcon.ic_trash_bin_28,
        position = position,
        secondaryIcon = false
    )

    class SearchEngine(
        engine: com.tonapps.wallet.data.core.SearchEngine,
        position: ListCell.Position
    ): Text(
        titleRes = Localization.search,
        value = engine.title,
        position = position
    )

    class Rate(
        position: ListCell.Position
    ): Icon(
        titleRes = Localization.rate_tonkeeper,
        iconRes = UIKitIcon.ic_star_28,
        position = position,
        secondaryIcon = true
    )
}