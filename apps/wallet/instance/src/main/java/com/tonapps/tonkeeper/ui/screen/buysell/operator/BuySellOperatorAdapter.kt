package com.tonapps.tonkeeper.ui.screen.buysell.operator

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import com.tonapps.tonkeeper.api.buysell.BuySellOperator
import com.tonapps.tonkeeper.ui.screen.swap.CellBackgroundDrawable
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.UIKitColor
import com.tonapps.uikit.color.resolveColor
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.wallet.localization.Localization
import uikit.widget.FrescoView


open class BuySellOperatorAdapter(private val listener: (BuySellOperator) -> Unit) :
    BaseListAdapter() {

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        return OperatorHolder(parent, listener)
    }
}

class OperatorHolder(parent: ViewGroup, private val listener: (BuySellOperator) -> Unit) :
    BaseListHolder<BuySellOperator>(parent, R.layout.view_cell_buysell_operator) {

    private val titleView = findViewById<AppCompatTextView>(R.id.title)
    private val subTitleView = findViewById<AppCompatTextView>(R.id.subtitle)
    private val radio = findViewById<AppCompatRadioButton>(R.id.radio)
    private val iconView = findViewById<FrescoView>(R.id.icon)
    private val bestView = findViewById<View>(R.id.best)

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

    override fun onBind(item: BuySellOperator) {
        itemView.background = CellBackgroundDrawable.create(context, bindingAdapterPosition, bindingAdapter?.itemCount ?: 1)
        itemView.setOnClickListener {
            listener(item)
        }
        radio.isChecked = item.selected
        bestView.isVisible = item.best
        iconView.setImageURI(item.logo, this)
        titleView.text = item.name
        subTitleView.text = item.rate.toString()
        val rate = "%.2f".format(item.rate).replace(',', '.')

        subTitleView.text = context.getString(Localization.rate_for, "$rate ${item.currency}", "1 TON")
    }

}