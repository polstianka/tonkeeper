package com.tonapps.tonkeeper.fragment.trade.ui.rv.mapper

import com.tonapps.tonkeeper.fragment.trade.domain.model.BuyMethod
import com.tonapps.tonkeeper.fragment.trade.ui.rv.model.TradeMethodListItem
import com.tonapps.uikit.list.ListCell

class BuyMethodMapper {

    fun map(
        model: BuyMethod,
        index: Int,
        listSize: Int
    ): TradeMethodListItem {
        return with(model) {
            TradeMethodListItem(
                id,
                false,
                name,
                iconUrl,
                position = ListCell.getPosition(listSize, index)
            )
        }
    }
}