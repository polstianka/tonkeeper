package com.tonapps.tonkeeper.ui.screen.battery.settings.list.holder

import android.view.ViewGroup
import com.tonapps.tonkeeper.ui.screen.battery.settings.list.Item
import com.tonapps.tonkeeperx.R

class SettingsHeaderHolder(
    parent: ViewGroup
): Holder<Item.SettingsHeader>(parent, R.layout.view_battery_settings_header) {

    override fun onBind(item: Item.SettingsHeader) {}
}
