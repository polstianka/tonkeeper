package com.tonapps.tonkeeper.ui.screen.swap.assets.list

import androidx.annotation.StringRes
import com.tonapps.tonkeeper.ui.screen.swap.data.AssetEntity
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell

enum class ListItemType {
    HEADER,
    SUGGESTED_TOKENS_LIST,
    TOKEN,
    TOKEN_PATCH,
    EMPTY;

    companion object {
        fun fromOrdinal(ordinal: Int): ListItemType {
            val itemType = when (ordinal) {
                HEADER.ordinal -> HEADER
                SUGGESTED_TOKENS_LIST.ordinal -> SUGGESTED_TOKENS_LIST
                TOKEN.ordinal -> TOKEN
                TOKEN_PATCH.ordinal -> TOKEN_PATCH
                EMPTY.ordinal -> EMPTY
                else -> error("Unknown ordinal: $ordinal")
            }
            return when (itemType) { // compile-level check
                HEADER,
                SUGGESTED_TOKENS_LIST,
                TOKEN,
                TOKEN_PATCH,
                EMPTY -> itemType
            }
        }
    }
}

abstract class Item(val itemType: ListItemType): BaseListItem(itemType.ordinal)
data class HeaderItem(@StringRes val titleRes: Int) : Item(ListItemType.HEADER)
data class EmptyItem(@StringRes val emptyRes: Int) : Item(ListItemType.EMPTY)
data class SuggestedTokensItem(val list: List<TokenPatchItem>) : Item(ListItemType.SUGGESTED_TOKENS_LIST)
data class TokenItem(val entity: AssetEntity, val position: ListCell.Position) : Item(ListItemType.TOKEN)
data class TokenPatchItem(val entity: AssetEntity, val position: ListCell.Position) : Item(ListItemType.TOKEN_PATCH)