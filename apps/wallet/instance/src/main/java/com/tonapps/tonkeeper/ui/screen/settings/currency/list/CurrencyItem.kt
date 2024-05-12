package com.tonapps.tonkeeper.ui.screen.settings.currency.list

import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell

data class CurrencyItem(
    val currency: String,
    val nameResId: Int,
    val selected: Boolean,
    val position: ListCell.Position
): BaseListItem()