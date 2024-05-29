package com.tonapps.tonkeeper.fragment.swap.pick_asset.rv

import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAssetBalance
import com.tonapps.uikit.list.ListCell

class TokenListItemMapper {

    private val cache = mutableMapOf<DexAssetBalance, TokenListItem>()

    fun map(
        item: DexAssetBalance,
        itemType: TokenItemType,
        size: Int,
        index: Int
    ): TokenListItem {
        if (!cache.containsKey(item)) {
            cache[item] = TokenListItem(
                model = item,
                iconUri = item.imageUri,
                symbol = item.symbol,
                name = item.displayName,
                position = ListCell.getPosition(size, index),
                itemType = itemType
            )
        }
        var result = cache[item]!!
        val position = ListCell.getPosition(size, index)
        if (result.position != position) {
            result = result.copy(position = position)
        }
        if (result.itemType != itemType) {
            result = result.copy(itemType = itemType)
        }
        return result
    }
}