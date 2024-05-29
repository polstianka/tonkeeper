package com.tonapps.tonkeeper.ui.screen.swap.assets.list

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.uikit.list.ListCell
import uikit.R
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.isRtl

class Decoration(context: Context): RecyclerView.ItemDecoration() {
    private val offsetMedium = context.getDimensionPixelSize(R.dimen.offsetMedium)
    private val offsetMediumHalf = context.getDimensionPixelSize(R.dimen.offsetMediumHalf)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val item = view.tag as? Item ?: return

        when (item.itemType) {
            ListItemType.HEADER, ListItemType.SUGGESTED_TOKENS_LIST, ListItemType.EMPTY -> { }
            ListItemType.TOKEN -> {
                require(item is TokenItem)
                outRect.left = offsetMedium
                outRect.right = offsetMedium
            }
            ListItemType.TOKEN_PATCH -> {
                require(item is TokenPatchItem)
                val isRtl = parent.isRtl()
                when (item.position) {
                    ListCell.Position.SINGLE -> {
                        outRect.left = offsetMedium
                        outRect.right = offsetMedium
                    }
                    ListCell.Position.FIRST -> {
                        if (isRtl) {
                            outRect.right = offsetMedium
                        } else {
                            outRect.left = offsetMedium
                        }
                    }
                    ListCell.Position.MIDDLE -> {
                        if (isRtl) {
                            outRect.right = offsetMediumHalf
                        } else {
                            outRect.left = offsetMediumHalf
                        }
                    }
                    ListCell.Position.LAST -> {
                        if (isRtl) {
                            outRect.right = offsetMediumHalf
                            outRect.left = offsetMedium
                        } else {
                            outRect.right = offsetMedium
                            outRect.left = offsetMediumHalf
                        }
                    }
                }
            }
        }
    }
}