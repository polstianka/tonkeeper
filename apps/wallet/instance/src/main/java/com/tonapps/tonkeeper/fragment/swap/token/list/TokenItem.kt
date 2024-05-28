package com.tonapps.tonkeeper.fragment.swap.token.list

import com.tonapps.tonkeeper.fragment.swap.model.TokenInfo

data class TokenItem(
    val tokenInfo: TokenInfo,
    override val position: com.tonapps.uikit.list.ListCell.Position,
    val selected: Boolean = false,
): com.tonapps.uikit.list.BaseListItem(), com.tonapps.uikit.list.ListCell