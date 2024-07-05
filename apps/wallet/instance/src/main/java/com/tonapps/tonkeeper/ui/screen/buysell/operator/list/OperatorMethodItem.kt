package com.tonapps.tonkeeper.ui.screen.buysell.operator.list

import com.tonapps.tonkeeper.core.fiat.models.FiatItem
import com.tonapps.wallet.data.rates.entity.OperatorBuyRateEntity

data class OperatorMethodItem(
    val body: FiatItem,
    val rate: OperatorBuyRateEntity?,
    override val position: com.tonapps.uikit.list.ListCell.Position
): com.tonapps.uikit.list.BaseListItem(), com.tonapps.uikit.list.ListCell {

    val id: String
        get() = body.id

    val title: String
        get() = body.title

    val subtitle: String
        get() = body.subtitle

    val iconUrl: String
        get() = body.iconUrl
}