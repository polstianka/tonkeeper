package com.tonapps.tonkeeper.fragment.swap.currency.list.holder

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.fragment.swap.currency.list.SwapDetailsItem
import com.tonapps.tonkeeperx.R

class SwapDetailsCellHolder(
    parent: ViewGroup,
    private val onClick: (item: SwapDetailsItem.Cell) -> Unit
): SwapDetailsHolder<SwapDetailsItem.Cell>(parent, R.layout.view_cell_swap_details) {

    private val titleView = findViewById<AppCompatTextView>(R.id.title)
    private val valueView = findViewById<AppCompatTextView>(R.id.value)
    private val additionalInfoView = findViewById<AppCompatImageView>(R.id.additional_info)

    override fun onBind(item: SwapDetailsItem.Cell) {
        itemView.setOnClickListener { onClick(item) }
        titleView.text = getString(item.title)
        valueView.text = item.value
        additionalInfoView.visibility = if (item.additionalInfo == null) View.GONE else View.VISIBLE
    }

}
