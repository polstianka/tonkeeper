package com.tonapps.tonkeeper.fragment.stake.confirm.rv

import com.tonapps.tonkeeper.core.TextWrapper
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell

data class ConfirmStakeListItem(
    val name: TextWrapper,
    val textPrimary: TextWrapper,
    val textSecondary: String? = null,
    val position: ListCell.Position,
    val itemType: ConfirmStakeItemType
) : BaseListItem(1)
