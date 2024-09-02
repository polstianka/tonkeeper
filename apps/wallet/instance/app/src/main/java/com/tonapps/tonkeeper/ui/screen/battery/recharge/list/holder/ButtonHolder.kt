package com.tonapps.tonkeeper.ui.screen.battery.recharge.list.holder

import android.view.ViewGroup
import android.widget.Button
import com.tonapps.tonkeeper.ui.screen.battery.recharge.list.Item
import com.tonapps.tonkeeperx.R

class ButtonHolder(
    parent: ViewGroup,
    private val onContinue: () -> Unit,
) : Holder<Item.Button>(parent, R.layout.fragment_recharge_button) {

    private val buttonView = itemView.findViewById<Button>(R.id.button)

    override fun onBind(item: Item.Button) {
        buttonView.setOnClickListener {
            onContinue()
        }
        buttonView.isEnabled = item.isEnabled
    }
}