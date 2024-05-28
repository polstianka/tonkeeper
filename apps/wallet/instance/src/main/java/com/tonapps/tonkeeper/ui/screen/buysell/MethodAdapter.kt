package com.tonapps.tonkeeper.ui.screen.buysell

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem
import uikit.extensions.drawable
import uikit.widget.ActionCellRadioView

class MethodAdapter(
    private val onItemSelected: (String) -> Unit
) : BaseListAdapter() {
    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        val cell = ActionCellRadioView(parent.context)
        val lp = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        cell.layoutParams = lp
        return MethodViewHolder(cell, onItemSelected)
    }
}

class MethodViewHolder(
    private val view: ActionCellRadioView,
    private val onItemSelected: (String) -> Unit
) : BaseListHolder<Method>(view) {

    private val radioView: AppCompatRadioButton = findViewById(uikit.R.id.action_cell_radio_button)
    private val title: AppCompatTextView = findViewById(uikit.R.id.action_cell_title)
    private val subtitle: AppCompatTextView = findViewById(uikit.R.id.action_cell_subtitle)
    private val icon: SimpleDraweeView = findViewById(uikit.R.id.action_cell_icon)

    override fun onBind(item: Method) {
        icon.setImageURI(item.iconUrl)
        view.iconVisible = true
        radioView.isChecked = item.selected
        title.text = item.name
        subtitle.text = item.subtitle
        itemView.background = item.position.drawable(context)
        itemView.setOnClickListener {
            onItemSelected(item.id)
        }
        radioView.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                onItemSelected(item.id)
            }
        }
    }

}