package com.tonapps.tonkeeper.ui.screen.notifications.list.holder

import android.view.ViewGroup
import com.tonapps.tonkeeper.koin.settingsRepository
import com.tonapps.tonkeeper.ui.screen.notifications.list.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.settings.SettingsRepository
import uikit.extensions.drawable
import uikit.widget.SwitchView

class WalletPushHolder(parent: ViewGroup): Holder<Item.Wallet>(parent, R.layout.view_notifications_wallet) {

    private val settingsRepository: SettingsRepository? by lazy { context.settingsRepository }
    private val switchView = findViewById<SwitchView>(R.id.push)

    init {
        itemView.background = ListCell.Position.SINGLE.drawable(context)
        itemView.setOnClickListener {
            switchView.toggle()
        }
    }

    override fun onBind(item: Item.Wallet) {
        switchView.checked = item.pushEnabled
        switchView.doCheckedChanged = { settingsRepository?.setPushWallet(item.walletId, it) }
    }
}