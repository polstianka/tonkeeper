package com.tonapps.tonkeeper.ui.screen.swap.choose

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import com.tonapps.blockchain.Coin
import com.tonapps.tonkeeper.api.swap.StonfiSwapAsset
import com.tonapps.tonkeeper.api.swap.SwapRepository
import com.tonapps.tonkeeper.ui.screen.swap.CellBackgroundDrawable
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.resolveColor
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem
import uikit.widget.FrescoView

open class SwapAssetAdapter(private val listener: (StonfiSwapAsset) -> Unit) : BaseListAdapter() {

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        return SwapAssetHolder(parent, listener)
    }

}

class SwapAssetHolder(parent: ViewGroup, private val listener: (StonfiSwapAsset) -> Unit) :
    BaseListHolder<StonfiSwapAsset>(parent, R.layout.view_cell_jetton) {

    private val titleView = findViewById<AppCompatTextView>(R.id.title)
    private val subTitleView = findViewById<AppCompatTextView>(R.id.rate)
    private val balanceView = findViewById<AppCompatTextView>(R.id.balance)
    private val balanceCurView = findViewById<AppCompatTextView>(R.id.balance_currency)
    private val iconView = findViewById<FrescoView>(R.id.icon)
    private val tonView = findViewById<AppCompatTextView>(R.id.ton)

    override fun onBind(item: StonfiSwapAsset) {
        itemView.background = CellBackgroundDrawable.create(context, bindingAdapterPosition, bindingAdapter?.itemCount ?: 1)
        itemView.setOnClickListener {
            listener(item)
        }
        iconView.setImageURI(item.imageURL, this)
        titleView.text = item.symbol
        subTitleView.text = item.displayName
        val balanceLong = item.balance.toLongOrNull() ?: 0L
        if (balanceLong > 0) {
            balanceView.text = Coin.toCoins(balanceLong, item.decimals).toString()
            balanceView.setTextColor(context.resolveColor(com.tonapps.uikit.color.R.attr.textPrimaryColor))
        } else {
            balanceView.text = "0"
            balanceView.setTextColor(context.resolveColor(com.tonapps.uikit.color.R.attr.textTertiaryColor))
        }
        balanceCurView.text = item.balanceInCurrency
        if (item.contractAddress == SwapRepository.USDT_ADDRESS) {
            tonView.isVisible = true
            tonView.text = "TON"
        } else {
            tonView.isVisible = false
        }
    }

}