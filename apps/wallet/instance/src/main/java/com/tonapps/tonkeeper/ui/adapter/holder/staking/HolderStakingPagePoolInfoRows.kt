package com.tonapps.tonkeeper.ui.adapter.holder.staking

import android.view.ViewGroup
import androidx.core.view.isVisible
import com.tonapps.tonkeeper.fragment.swap.view.SwapInfoRowView
import com.tonapps.tonkeeper.ui.adapter.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.BaseListHolder

class HolderStakingPagePoolInfoRows(
    parent: ViewGroup
) : BaseListHolder<Item.StakingPagePoolInfoRows>(parent, R.layout.holder_pool_rows) {
    private val apyView = findViewById<SwapInfoRowView>(R.id.row_apy)
    private val depositView = findViewById<SwapInfoRowView>(R.id.row_deposit)

    override fun onBind(item: Item.StakingPagePoolInfoRows) {
        apyView.labelView.isVisible = item.isMaxApy
        apyView.setValue(item.apy)
        depositView.setValue(item.minimalDeposit)
    }
}