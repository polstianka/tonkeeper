package com.tonapps.tonkeeper.fragment.stake.confirm.rv

import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import com.tonapps.tonkeeper.core.toString
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.BaseListHolder
import uikit.widget.item.BaseItemView

class ConfirmStakeHolder(
    parent: ViewGroup
) : BaseListHolder<ConfirmStakeListItem>(parent, R.layout.view_confirm_stake_item) {

    private val baseItemView = itemView as BaseItemView
    private val name: TextView = findViewById(R.id.view_confirm_stake_item_name)
    private val textPrimary: TextView = findViewById(R.id.view_confirm_stake_item_text_primary)
    private val textSecondary: TextView = findViewById(R.id.view_confirm_stake_item_text_secondary)
    override fun onBind(item: ConfirmStakeListItem) {
        baseItemView.position = item.position
        name.text = context.toString(item.name)
        textPrimary.text = context.toString(item.textPrimary)
        textSecondary.isVisible = item.textSecondary != null
        item.textSecondary?.let { textSecondary.text = it }
    }
}