package com.tonapps.tonkeeper.ui.screen.picker.list

import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.account.WalletType
import com.tonapps.wallet.data.account.entities.WalletLabel

sealed class Item(type: Int): BaseListItem(type) {
    companion object {
        const val TYPE_WALLET = 0
        const val TYPE_ADD_WALLET = 1
        const val TYPE_SKELETON = 2
    }

    data class Skeleton(
        val position: ListCell.Position
    ): Item(TYPE_SKELETON)

    data class Wallet(
        val accountId: String,
        val walletId: Long,
        val walletLabel: WalletLabel,
        val walletType: WalletType,
        val selected: Boolean,
        val position: ListCell.Position,
        val balance: CharSequence,
        val hiddenBalance: Boolean
    ): Item(TYPE_WALLET) {

        val color: Int
            get() = walletLabel.color

        val emoji: CharSequence
            get() = walletLabel.emoji

        val name: String
            get() = walletLabel.name

        val testnet: Boolean
            get() = walletType == WalletType.Testnet
    }

    data object AddWallet: Item(TYPE_ADD_WALLET)
}