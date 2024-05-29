package com.tonapps.tonkeeper.ui.screen.buysell.amount

import android.content.res.ColorStateList
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.api.buysell.BuySellType
import com.tonapps.tonkeeper.ui.screen.swap.CellBackgroundDrawable
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.UIKitColor
import com.tonapps.uikit.color.resolveColor
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem


open class BuySellTypeAdapter(private val listener: (BuySellType) -> Unit) : BaseListAdapter() {

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        return TypeHolder(parent, listener)
    }
}

class TypeHolder(parent: ViewGroup, private val listener: (BuySellType) -> Unit) :
    BaseListHolder<BuySellType>(parent, R.layout.view_cell_buysell_type) {

    private val titleView = findViewById<AppCompatTextView>(R.id.title)
    private val radio = findViewById<AppCompatRadioButton>(R.id.radio)
    private val iconView = findViewById<AppCompatImageView>(R.id.icon)

    init {
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
            ),
            intArrayOf(
                context.resolveColor(UIKitColor.buttonTertiaryBackgroundColor),
                context.resolveColor(UIKitColor.buttonPrimaryBackgroundColor),
            )
        )
        radio.buttonTintList = colorStateList
    }

    override fun onBind(item: BuySellType) {
        itemView.background = CellBackgroundDrawable.create(context, bindingAdapterPosition, bindingAdapter?.itemCount ?: 1)
        itemView.setOnClickListener {
            listener(item)
        }
        radio.isChecked = item.selected
        titleView.text = item.title
        iconView.setImageResource(item.iconRes)
    }

}