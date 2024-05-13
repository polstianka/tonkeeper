package com.tonapps.tonkeeper.fragment.trade.ui.rv.mapper

import com.tonapps.tonkeeper.fragment.trade.domain.model.ExchangeMethod
import com.tonapps.tonkeeper.fragment.trade.ui.rv.model.ExchangeMethodListItem
import com.tonapps.uikit.list.ListCell

class ExchangeMethodMapper {

    fun map(
        model: ExchangeMethod,
        index: Int,
        listSize: Int
    ): ExchangeMethodListItem {
        return with(model) {
            ExchangeMethodListItem(
                id,
                false,
                name,
                iconUrl,
                position = ListCell.getPosition(listSize, index)
            )
        }
    }
}