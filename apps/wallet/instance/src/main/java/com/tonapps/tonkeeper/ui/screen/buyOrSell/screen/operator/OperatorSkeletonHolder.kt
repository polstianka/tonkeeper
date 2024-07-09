package com.tonapps.tonkeeper.ui.screen.buyOrSell.screen.operator

import android.view.ViewGroup
import com.tonapps.tonkeeper.ui.screen.wallet.list.Item
import com.tonapps.tonkeeper.ui.screen.wallet.list.holder.Holder
import com.tonapps.tonkeeperx.R

class OperatorSkeletonHolder(
    parent: ViewGroup,
): Holder<Item.Skeleton>(parent,  R.layout.view_operator_skeleton) {
    override fun onBind(item: Item.Skeleton) {

    }
}