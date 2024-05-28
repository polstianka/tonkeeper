package com.tonapps.tonkeeper.fragment.swap.currency.list.holder

import android.view.ViewGroup
import com.tonapps.tonkeeper.fragment.swap.currency.list.SwapDetailsItem
import com.tonapps.tonkeeperx.R

class SwapDetailsDividerHolder(
    parent: ViewGroup
): SwapDetailsHolder<SwapDetailsItem.Divider>(parent, R.layout.view_divider) {
    override fun onBind(item: SwapDetailsItem.Divider) = Unit
}