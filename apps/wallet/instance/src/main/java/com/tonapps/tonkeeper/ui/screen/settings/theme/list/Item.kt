package com.tonapps.tonkeeper.ui.screen.settings.theme.list

import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.core.Theme

class Item(
    val position: ListCell.Position,
    val theme: Theme,
    val selected: Boolean
): BaseListItem(0)