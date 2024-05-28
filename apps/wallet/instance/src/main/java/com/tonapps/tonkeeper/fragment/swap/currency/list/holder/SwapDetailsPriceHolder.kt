package com.tonapps.tonkeeper.fragment.swap.currency.list.holder

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.fragment.swap.currency.list.SwapDetailsItem
import com.tonapps.tonkeeperx.R

class SwapDetailsPriceHolder(
    parent: ViewGroup,
    private val onClick: (item: SwapDetailsItem) -> Unit
): SwapDetailsHolder<SwapDetailsItem.Header>(parent, R.layout.view_cell_swap_price_details) {

    private val titleView = findViewById<AppCompatTextView>(R.id.title)
    private val arrowView = findViewById<AppCompatImageView>(R.id.arrow)
    private val loadingView = findViewById<View>(R.id.progress_bar)
    private var expanded = true

    override fun onBind(item: SwapDetailsItem.Header) {
        itemView.setOnClickListener {
            onClick(item)
            arrowView.rotation = if (expanded) 0F else 180F
            expanded = !expanded
        }

        titleView.text = item.title
        loadingView.visibility = if (item.loading) View.VISIBLE else View.GONE
        arrowView.visibility = if (item.loading) View.GONE else View.VISIBLE
    }
}
