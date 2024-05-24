package com.tonapps.tonkeeper.fragment.stake.pick_option.rv

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem
import uikit.widget.item.BaseItemView

abstract class BaseStakingOptionHolder<I : BaseListItem> (
    parent: ViewGroup
) : BaseListHolder<I>(parent, R.layout.view_item_staking_option) {

    protected val baseItemView: BaseItemView = itemView as BaseItemView
    protected val icon: ImageView = findViewById(R.id.view_item_staking_option_icon)
    protected val title: TextView = findViewById(R.id.view_item_staking_option_title)
    protected val subtitle: TextView = findViewById(R.id.view_item_staking_option_subtitle)
    protected val radioButton: RadioButton = findViewById(R.id.view_item_staking_option_radiobutton)
    protected val chip: View = findViewById(R.id.view_item_staking_option_chip)
}