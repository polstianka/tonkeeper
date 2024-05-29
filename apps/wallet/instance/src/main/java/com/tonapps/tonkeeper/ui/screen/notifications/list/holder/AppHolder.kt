package com.tonapps.tonkeeper.ui.screen.notifications.list.holder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.koin.tonConnectRepository
import com.tonapps.tonkeeper.ui.screen.notifications.list.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.data.tonconnect.TonConnectRepository
import uikit.extensions.drawable
import uikit.widget.FrescoView
import uikit.widget.SwitchView

class AppHolder(parent: ViewGroup): Holder<Item.App>(parent, R.layout.view_notifications_app) {

    private val tonConnectRepository: TonConnectRepository? by lazy { context.tonConnectRepository }

    private val iconView = findViewById<FrescoView>(R.id.icon)
    private val titleView = findViewById<AppCompatTextView>(R.id.title)
    private val pushView = findViewById<SwitchView>(R.id.push)

    init {
        itemView.setOnClickListener {
            pushView.toggle()
        }
    }

    override fun onBind(item: Item.App) {
        itemView.background = item.position.drawable(context)
        iconView.setImageURI(item.icon, null)
        titleView.text = item.name
        pushView.checked = item.pushEnabled
        pushView.doCheckedChanged = { isChecked ->
            tonConnectRepository?.setPushEnabled(item.walletId, item.url, isChecked)
        }
    }

}