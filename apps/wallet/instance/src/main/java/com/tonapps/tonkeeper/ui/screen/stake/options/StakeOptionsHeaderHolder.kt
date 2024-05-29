package com.tonapps.tonkeeper.ui.screen.stake.options


import android.view.ViewGroup
import com.tonapps.tonkeeperx.R
import uikit.extensions.setPaddingVertical
import uikit.widget.TitleView

class StakeOptionsHeaderHolder(parent: ViewGroup) :
    StakeOptionsHolder<OptionItem.Header>(parent, R.layout.view_history_header) {

    private val titleView = findViewById<TitleView>(R.id.title)

    init {
        titleView.setPaddingVertical(0)
    }

    override fun onBind(item: OptionItem.Header) {
        titleView.text = item.title
    }

}