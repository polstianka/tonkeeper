package com.tonapps.tonkeeper.ui.adapter

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import uikit.extensions.dp

object ItemDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val holder = parent.getChildViewHolder(view)

        when (holder.itemViewType) {
            Item.Type.TITLE_H3.value -> outRect.set(2.dp, 14.dp, 2.dp, 14.dp)
            Item.Type.TITLE_LABEL1.value -> outRect.set(2.dp, 12.dp, 2.dp, 12.dp)
            Item.Type.DESCRIPTION_BODY3.value -> outRect.set(1.dp, 12.dp, 1.dp, 16.dp)

            Item.Type.LINKS.value -> outRect.bottom = 16.dp
            Item.Type.TOKEN_SUGGESTIONS.value -> outRect.bottom = 16.dp

            Item.Type.STAKING_PAGE_HEADER.value -> outRect.set(4.dp, 16.dp, 4.dp, 16.dp)
            Item.Type.STAKING_PAGE_ACTIONS.value -> outRect.bottom = 24.dp
            Item.Type.STAKING_PAGE_CHART_HEADER.value -> outRect.set(12.dp, 28.dp, 12.dp, 0.dp)
            Item.Type.STAKING_PAGE_CHART_PERIOD.value -> outRect.bottom = 8.dp
            Item.Type.STAKING_PAGE_POOL_INFO_ROWS.value -> outRect.top = 8.dp

            Item.Type.OFFSET_8DP.value -> outRect.top = 8.dp
            Item.Type.OFFSET_16DP.value -> outRect.top = 16.dp
            Item.Type.OFFSET_32DP.value -> outRect.top = 32.dp
        }
    }
}