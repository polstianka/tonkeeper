package com.tonapps.tonkeeper.ui.screen.battery.refill.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.ui.screen.battery.refill.list.holder.BatteryHolder
import com.tonapps.tonkeeper.ui.screen.battery.refill.list.holder.GiftHolder
import com.tonapps.tonkeeper.ui.screen.battery.refill.list.holder.RechargeMethodHolder
import com.tonapps.tonkeeper.ui.screen.battery.refill.list.holder.SettingsHolder
import com.tonapps.tonkeeper.ui.screen.battery.refill.list.holder.SpaceHolder
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

class Adapter(
    private val openSettings: () -> Unit
): BaseListAdapter() {
    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        return when(viewType) {
            Item.TYPE_BATTERY -> BatteryHolder(parent, openSettings)
            Item.TYPE_SPACE -> SpaceHolder(parent)
            Item.TYPE_RECHARGE_METHOD -> RechargeMethodHolder(parent)
            Item.TYPE_GIFT -> GiftHolder(parent)
            Item.TYPE_SETTINGS -> SettingsHolder(parent, openSettings)
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.isNestedScrollingEnabled = true
    }
}