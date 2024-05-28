package com.tonapps.tonkeeper.fragment.swap.token.suggestions

import com.tonapps.tonkeeper.fragment.swap.model.TokenInfo

data class SuggestionTokenItem(
    val tokenInfo: TokenInfo,
) : com.tonapps.uikit.list.BaseListItem()