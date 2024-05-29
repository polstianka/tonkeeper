package com.tonapps.tonkeeper.ui.adapter

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import uikit.extensions.horizontal

class ItemHorizontalDecoration(private val horizontalOffset: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val holder = parent.getChildViewHolder(view)

        if (holder.itemViewType == Item.Type.STAKING_PAGE_CHART.value) {
            return
        }

        outRect.horizontal = horizontalOffset
    }
}