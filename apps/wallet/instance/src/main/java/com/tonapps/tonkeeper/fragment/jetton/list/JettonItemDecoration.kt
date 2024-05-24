package com.tonapps.tonkeeper.fragment.jetton.list

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.core.history.list.holder.HistoryHolder
import uikit.R
import uikit.extensions.dp
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.horizontal
import uikit.extensions.vertical

class JettonItemDecoration(
    context: Context
): RecyclerView.ItemDecoration() {

    private val offset = context.getDimensionPixelSize(R.dimen.offsetMedium)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val holder = parent.getChildViewHolder(view)
        if (holder is HistoryHolder<*>) {
            outRect.horizontal = offset
            outRect.vertical = 6.dp
        }
    }
}