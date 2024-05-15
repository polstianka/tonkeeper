package com.tonapps.tonkeeper.ui.component.keyvalue

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem
import uikit.extensions.dp
import uikit.extensions.setPaddingHorizontal
import uikit.widget.ColumnLayout
import uikit.widget.RowLayout

class KeyValueRowAdapter : BaseListAdapter() {
    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        return when (viewType) {
            SIMPLE_VIEW_TYPE -> {
                val rowLayout = RowLayout(parent.context).apply {
                    layoutParams = RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setPaddingHorizontal(16.dp)
                }
                KeyValueSimpleRowHolder(rowLayout)
            }

            HEADER_VIEW_TYPE -> {
                val columnLayout = ColumnLayout(parent.context).apply {
                    layoutParams = RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setPaddingHorizontal(16.dp)
                }
                KeyValueHeaderRowHolder(columnLayout)
            }

            else -> error("unknown type")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (currentList[position]) {
            is KeyValueModel.Simple -> SIMPLE_VIEW_TYPE
            is KeyValueModel.Header -> HEADER_VIEW_TYPE
            else -> error("unknown type")
        }
    }

    private companion object {
        private const val SIMPLE_VIEW_TYPE = 0
        private const val HEADER_VIEW_TYPE = 1
    }
}