package com.tonapps.tonkeeper.ui.screen.battery.list.holder

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.ui.screen.backup.main.BackupScreen
import com.tonapps.tonkeeper.ui.screen.battery.list.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.localization.Localization
import uikit.extensions.drawable
import uikit.widget.FrescoView

class RechargeMethodHolder(
    parent: ViewGroup,
): Holder<Item.RechargeMethod>(parent, R.layout.view_cell_recharge_method) {

    private val frescoView = itemView.findViewById<FrescoView>(R.id.icon)
    private val titleView = itemView.findViewById<AppCompatTextView>(R.id.title)

    override fun onBind(item: Item.RechargeMethod) {
        itemView.background = item.position.drawable(context)
        itemView.setOnClickListener {
            navigation?.add(BackupScreen.newInstance())
        }
        frescoView.setImageURI(item.image, this)
        frescoView.visibility = View.VISIBLE
        titleView.text = context.getString(Localization.battery_refill_crypto, item.currency)
    }
}