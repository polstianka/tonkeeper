package com.tonapps.tonkeeper.ui.screen.buysell.country.list

import com.tonapps.tonkeeper.ui.screen.buysell.country.BuySellCountryScreenFeature


data class BuySellCountryItem(
    val code: String,
    val title: String,
    val emoji: String,
    val selected: Boolean,
    override val position: com.tonapps.uikit.list.ListCell.Position
): com.tonapps.uikit.list.BaseListItem(), com.tonapps.uikit.list.ListCell {

    constructor(
        country: BuySellCountryScreenFeature.Country,
        selected: Boolean,
        position: com.tonapps.uikit.list.ListCell.Position
    ) : this(
        country.code,
        country.title,
        country.emoji,
        selected,
        position
    )
}