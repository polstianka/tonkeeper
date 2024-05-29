package com.tonapps.tonkeeper.ui.adapter.holder

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.ui.adapter.Adapter
import com.tonapps.tonkeeper.ui.adapter.Item
import com.tonapps.uikit.list.BaseListHolder
import uikit.extensions.dp

class HolderTokenSuggestions(parent: ViewGroup) :
    BaseListHolder<Item.TokenSuggestions>(RecyclerView(parent.context)) {

    private val listView: RecyclerView = itemView as RecyclerView
    private val adapter = Adapter()

    init {
        listView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        listView.addItemDecoration(SuggestionsItemDecorations)
        listView.adapter = adapter
    }

    override fun onBind(item: Item.TokenSuggestions) {
        adapter.submitList(item.list)
    }

    companion object {
        object SuggestionsItemDecorations : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                val position = parent.getChildAdapterPosition(view)
                if (position == 0) {
                    return
                }
                outRect.left = 8.dp
            }
        }
    }
}