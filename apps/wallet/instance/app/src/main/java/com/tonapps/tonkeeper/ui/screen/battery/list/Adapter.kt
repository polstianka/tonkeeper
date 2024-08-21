package com.tonapps.tonkeeper.ui.screen.battery.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.ui.screen.battery.list.holder.BatteryHolder
import com.tonapps.tonkeeper.ui.screen.battery.list.holder.GiftHolder
import com.tonapps.tonkeeper.ui.screen.battery.list.holder.RechargeMethodHolder
import com.tonapps.tonkeeper.ui.screen.battery.list.holder.SettingsHeaderHolder
import com.tonapps.tonkeeper.ui.screen.battery.list.holder.SettingsHolder
import com.tonapps.tonkeeper.ui.screen.battery.list.holder.SpaceHolder
import com.tonapps.tonkeeper.ui.screen.battery.list.holder.SupportedTransactionHolder
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.wallet.data.settings.BatteryTransaction

class Adapter(
    private val openSettings: () -> Unit
): BaseListAdapter() {

    override fun createHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseListHolder<out BaseListItem> {
        return when(viewType) {
            Item.TYPE_BATTERY -> BatteryHolder(parent, openSettings)
            Item.TYPE_RECHARGE_METHOD -> RechargeMethodHolder(parent)
            Item.TYPE_GIFT -> GiftHolder(parent)
            Item.TYPE_SPACE -> SpaceHolder(parent)
            Item.TYPE_SETTINGS -> SettingsHolder(parent, openSettings)
            Item.TYPE_SUPPORTED_TRANSACTION -> SupportedTransactionHolder(parent)
            Item.TYPE_SETTINGS_HEADER -> SettingsHeaderHolder(parent)
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.isNestedScrollingEnabled = true
    }

}