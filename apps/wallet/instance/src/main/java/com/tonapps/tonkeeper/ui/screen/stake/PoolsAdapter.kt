package com.tonapps.tonkeeper.ui.screen.stake

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.ui.screen.stake.model.PoolModel
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.wallet.localization.Localization
import uikit.widget.ActionCellRadioView

class PoolsAdapter(
    private val onClick: (PoolModel) -> Unit,
    private val onCheckedChanged: (String) -> Unit,
) : BaseListAdapter() {
    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        val cell = ActionCellRadioView(parent.context)
        val lp = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        cell.layoutParams = lp
        return PoolHolder(cell, onClick, onCheckedChanged)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.setHasFixedSize(false)
        recyclerView.isNestedScrollingEnabled = true
    }
}

class PoolHolder(
    private val view: ActionCellRadioView,
    private val onClick: (PoolModel) -> Unit,
    private val onCheckedChanged: (String) -> Unit,
) : BaseListHolder<PoolModel>(view) {

    override fun onBind(item: PoolModel) {
        view.title = item.name
        view.subtitle = context.getString(
            com.tonapps.wallet.localization.R.string.apy_percent_placeholder,
            item.apyFormatted
        )
        view.checked = item.selected
        view.titleBadgeText = if (item.isMaxApy) getString(Localization.max_apy) else null
        view.position = item.position
        view.setOnClickListener { onClick(item) }
        view.onCheckedChange = { onCheckedChanged(item.address) }
    }
}