package com.tonapps.tonkeeper.fragment.swap.token.suggestions

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import uikit.R
import uikit.extensions.getDimensionPixelSize

class SuggestionItemDecoration(
    context: Context
): RecyclerView.ItemDecoration() {

    private val offset = context.getDimensionPixelSize(R.dimen.cornerExtraSmall)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val holder = parent.getChildViewHolder(view)
        if (holder is SuggestionTokenHolder) {
            outRect.right = offset
        }
    }
}