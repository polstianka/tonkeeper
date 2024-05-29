package com.tonapps.tonkeeper.ui.screen.picker.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.account.entities.WalletEntity

class WalletPickerAdapter: BaseListAdapter() {

    companion object {

        private val defaultItems: List<Item> = listOf(
            Item.Skeleton(ListCell.getPosition(3, 0)),
            Item.Skeleton(ListCell.getPosition(3, 1)),
            Item.Skeleton(ListCell.getPosition(3, 2)),
            Item.AddWallet
        )

        fun map(
            wallets: List<WalletEntity>,
            activeWallet: WalletEntity,
            balances: List<CharSequence>,
            hiddenBalance: Boolean
        ): List<Item> {
            val uiItems = mutableListOf<Item>()
            for ((index, wallet) in wallets.withIndex()) {
                val item = Item.Wallet(
                    accountId = wallet.accountId,
                    walletId = wallet.id,
                    walletLabel = wallet.label,
                    walletType = wallet.type,
                    selected = wallet.id == activeWallet.id,
                    position = ListCell.getPosition(wallets.size, index),
                    balance = balances[index],
                    hiddenBalance = hiddenBalance
                )
                uiItems.add(item)
            }
            uiItems.add(Item.AddWallet)
            return uiItems
        }
    }

    init {
        submitList(defaultItems)
    }

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        return when(viewType) {
            Item.TYPE_WALLET -> Holder.Wallet(parent)
            Item.TYPE_ADD_WALLET -> Holder.AddWallet(parent)
            Item.TYPE_SKELETON -> Holder.Skeleton(parent)
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.setHasFixedSize(false)
        recyclerView.isNestedScrollingEnabled = true
    }
}