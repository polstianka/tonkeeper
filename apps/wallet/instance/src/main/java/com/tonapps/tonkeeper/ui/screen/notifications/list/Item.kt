package com.tonapps.tonkeeper.ui.screen.notifications.list

import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.tonconnect.entities.DAppEntity

sealed class Item(type: Int): BaseListItem(type) {

    companion object {
        const val TYPE_WALLET = 1
        const val TYPE_SPACE = 2
        const val TYPE_APPS_HEADER = 3
        const val TYPE_APP = 4
    }

    data class Wallet(
        val pushEnabled: Boolean,
        val walletId: Long,
    ): Item(TYPE_WALLET)

    data object Space: Item(TYPE_SPACE)

    data object AppsHeader: Item(TYPE_APPS_HEADER)

    data class App(
        val id: String,
        val name: String,
        val icon: String,
        val pushEnabled: Boolean,
        val position: ListCell.Position,
        val walletId: Long,
        val url: String,
    ): Item(TYPE_APP) {

        constructor(
            app: DAppEntity,
            position: ListCell.Position
        ) : this(
            id = app.uniqueId,
            name = app.manifest.name,
            icon = app.manifest.iconUrl,
            pushEnabled = app.enablePush,
            position = position,
            walletId = app.walletId,
            url = app.url,
        )
    }
}