package com.tonapps.tonkeeper.ui.screen.picker.list

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.emoji.ui.EmojiView
import com.tonapps.tonkeeper.koin.walletRepository
import com.tonapps.tonkeeper.ui.screen.add.AddScreen
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.icon.UIKitIcon
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.wallet.localization.Localization
import com.tonapps.wallet.data.account.WalletType
import com.tonapps.wallet.data.core.HIDDEN_BALANCE
import uikit.extensions.drawable
import uikit.navigation.Navigation
import uikit.navigation.Navigation.Companion.navigation

abstract class Holder<I: Item>(
    parent: ViewGroup,
    @LayoutRes resId: Int
): BaseListHolder<I>(parent, resId) {

    val navigation: Navigation?
        get() = context.navigation

    class Skeleton(
        parent: ViewGroup
    ): Holder<Item.Skeleton>(parent, R.layout.view_wallet_item) {

        init {
            findViewById<View>(R.id.wallet_color).visibility = View.GONE
        }

        override fun onBind(item: Item.Skeleton) {
            itemView.background = item.position.drawable(context)
        }
    }

    class Wallet(
        parent: ViewGroup
    ): Holder<Item.Wallet>(parent, R.layout.view_wallet_item) {

        private val colorView = findViewById<View>(R.id.wallet_color)
        private val emojiView = findViewById<EmojiView>(R.id.wallet_emoji)
        private val nameView = findViewById<AppCompatTextView>(R.id.wallet_name)
        private val balanceView = findViewById<AppCompatTextView>(R.id.wallet_balance)
        private val checkView = findViewById<AppCompatImageView>(R.id.check)
        private val typeView = findViewById<AppCompatTextView>(R.id.wallet_type)

        override fun onBind(item: Item.Wallet) {
            itemView.background = item.position.drawable(context)
            itemView.setOnClickListener {
                context.walletRepository?.chooseWallet(item.walletId)
            }

            colorView.backgroundTintList = ColorStateList.valueOf(item.color)
            emojiView.setEmoji(item.emoji)
            nameView.text = item.name
            if (item.hiddenBalance) {
                balanceView.text = HIDDEN_BALANCE
            } else {
                balanceView.text = item.balance
            }

            if (item.selected) {
                checkView.setImageResource(UIKitIcon.ic_donemark_otline_28)
            } else {
                checkView.setImageResource(0)
            }
            setType(item.walletType)
        }

        private fun setType(type: WalletType) {
            if (type == WalletType.Default) {
                typeView.visibility = View.GONE
                return
            }
            typeView.visibility = View.VISIBLE
            val resId = when (type) {
                WalletType.Watch -> Localization.watch_only
                WalletType.Testnet -> Localization.testnet
                WalletType.Signer -> Localization.signer
                else -> throw IllegalArgumentException("Unknown wallet type: $type")
            }
            typeView.setText(resId)
        }
    }

    class AddWallet(
        parent: ViewGroup
    ): Holder<Item.AddWallet>(parent, R.layout.view_wallet_add_item) {

        private val addButton = findViewById<View>(R.id.add)

        init {
            addButton.setOnClickListener {
                navigation?.add(AddScreen.newInstance())
            }
        }

        override fun onBind(item: Item.AddWallet) {

        }
    }

}
