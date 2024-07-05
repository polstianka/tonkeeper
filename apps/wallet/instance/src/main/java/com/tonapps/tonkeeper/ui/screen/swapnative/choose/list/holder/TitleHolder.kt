package com.tonapps.tonkeeper.ui.screen.swapnative.choose.list.holder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.ui.screen.swapnative.choose.list.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.BaseListHolder

class TitleHolder(
    parent: ViewGroup,
    private val onClick: (item: Item) -> Unit
) : BaseListHolder<Item.Title>(parent, R.layout.view_group_title) {

    private val titleView = findViewById<AppCompatTextView>(R.id.title)


    override fun onBind(item: Item.Title) {
        titleView.text = getString(item.title)
    }


}