package com.tonapps.tonkeeper.fragment.trade.ui.rv.mapper

import com.tonapps.tonkeeper.fragment.trade.domain.model.BuyMethod
import com.tonapps.tonkeeper.fragment.trade.ui.rv.model.TradeMethodListItem

class BuyMethodMapper {

    fun map(model: BuyMethod): TradeMethodListItem {
        return with(model) {
            TradeMethodListItem(id, false, name, iconUrl)
        }
    }
}