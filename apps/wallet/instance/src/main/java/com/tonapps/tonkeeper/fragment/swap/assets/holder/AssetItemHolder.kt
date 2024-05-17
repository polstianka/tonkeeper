package com.tonapps.tonkeeper.fragment.swap.assets.holder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeperx.R
import com.tonapps.tonkeeper.fragment.swap.assets.item.AssetItem
import com.tonapps.uikit.color.textPrimaryColor
import com.tonapps.uikit.color.textTertiaryColor
import com.tonapps.uikit.list.BaseListHolder
import uikit.extensions.addTag
import uikit.extensions.drawable
import uikit.spannable.SpanTag
import uikit.widget.FrescoView

class AssetItemHolder(
    parent: ViewGroup,
    private val clickListener: (symbol: String) -> Unit
): BaseListHolder<AssetItem.Item>(parent, R.layout.view_cell_asset) {

    private val iconView = findViewById<FrescoView>(R.id.icon)
    private val titleView = findViewById<AppCompatTextView>(R.id.title)
    private val subtitleView = findViewById<AppCompatTextView>(R.id.subtitle)

    private val balanceView = findViewById<AppCompatTextView>(R.id.balance)
    private val balanceFiatView = findViewById<AppCompatTextView>(R.id.balance_currency)

    private val textPrimaryColor = context.textPrimaryColor
    private val textTertiaryColor = context.textTertiaryColor

    private val tonTag = SpanTag(context, context.getString(com.tonapps.wallet.localization.R.string.ton))

    override fun onBind(item: AssetItem.Item) {
        itemView.background = item.position.drawable(context)
        itemView.setOnClickListener {
            clickListener.invoke(item.symbol)
        }
        iconView.setImageURI(item.icon, this)
        titleView.text = if (item.byTon) {
            item.symbol.addTag(tonTag)
        } else {
            item.symbol
        }

        balanceView.text = item.balanceFormat
        if (item.balanceFormat == "0") {
            balanceView.setTextColor(textTertiaryColor)
        } else {
            balanceView.setTextColor(textPrimaryColor)
        }
        balanceFiatView.text = item.balanceFiatFormat
        subtitleView.text = item.subtitle
    }

}