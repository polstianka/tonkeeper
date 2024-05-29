package com.tonapps.tonkeeper.ui.adapter.holder.staking

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.facebook.drawee.view.SimpleDraweeView
import com.tonapps.tonkeeper.ui.adapter.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.BaseListHolder

class HolderStakingPageHeader(
    parent: ViewGroup
) : BaseListHolder<Item.StakingPageHeader>(parent, R.layout.holder_staking_page_header) {

    private val iconView = findViewById<AppCompatImageView>(R.id.icon)
    private val iconLabel = findViewById<SimpleDraweeView>(R.id.icon_label)
    private val balanceView = findViewById<AppCompatTextView>(R.id.balance)
    private val currencyView = findViewById<AppCompatTextView>(R.id.currency_balance)

    override fun onBind(item: Item.StakingPageHeader) {
        iconView.setImageResource(com.tonapps.wallet.api.R.drawable.ic_ton_with_bg)
        iconLabel.setImageURI(item.iconUri)
        balanceView.text = item.balance
        currencyView.text = item.currencyBalance
    }
}