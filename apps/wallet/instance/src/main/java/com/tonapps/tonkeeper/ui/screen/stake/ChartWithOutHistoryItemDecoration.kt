package com.tonapps.tonkeeper.ui.screen.stake

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.fragment.chart.list.holder.ChartHeaderHolder
import com.tonapps.tonkeeper.fragment.chart.list.holder.ChartPriceHolder
import uikit.R
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.horizontal

class ChartWithOutHistoryItemDecoration(
    context: Context
) : RecyclerView.ItemDecoration() {

    private val offset = context.getDimensionPixelSize(R.dimen.offsetMedium)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val holder = parent.getChildViewHolder(view)
        if (holder is ChartHeaderHolder || holder is ChartPriceHolder) {
            outRect.horizontal = offset
        }
    }
}