package com.tonapps.tonkeeper.ui.screen.buysell.currency.list

import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell

data class BuySellCurrencyItem(
    val currency: String,
    val nameResId: Int,
    val selected: Boolean,
    val position: ListCell.Position
): BaseListItem()