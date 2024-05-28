package com.tonapps.tonkeeper.ui.screen.buysell

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem
import uikit.extensions.drawable

class MethodTypeAdapter(
    private val onItemSelected: (Int) -> Unit
) : BaseListAdapter() {
    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        return MethodTypeViewHolder(parent, onItemSelected)
    }
}

class MethodTypeViewHolder(parent: ViewGroup, private val onItemSelected: (Int) -> Unit) :
    BaseListHolder<FiatAmountUiState.MethodType>(parent, R.layout.method_type_item_view) {

    private val radioButton: AppCompatRadioButton = findViewById(R.id.radio_button)
    private val nameView: AppCompatTextView = findViewById(R.id.name)

    override fun onBind(item: FiatAmountUiState.MethodType) {
        radioButton.isChecked = item.selected
        nameView.text = item.name
        itemView.background = item.position.drawable(context)
        itemView.setOnClickListener {
            onItemSelected(item.id)
        }
        radioButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                onItemSelected(item.id)
            }
        }
    }
}