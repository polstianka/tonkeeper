package com.tonapps.tonkeeper.fragment.swap.assets.holder

import android.view.View
import android.view.ViewGroup
import com.tonapps.tonkeeper.fragment.swap.assets.item.AssetItem
import com.tonapps.uikit.list.BaseListHolder
import uikit.widget.LabelView

class AssetLabelHolder(
    parent: ViewGroup,
) : BaseListHolder<AssetItem.Label>(createView(parent)) {

    private val label = itemView as LabelView

    override fun onBind(item: AssetItem.Label) {
        label.setText(item.labelRes)
    }

    companion object {
        fun createView(parent: ViewGroup): View {
            val view = LabelView(parent.context)
            view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            return view
        }
    }

}