package com.tonapps.tonkeeper.fragment.swap.pick_asset.rv

import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAssetBalance
import com.tonapps.tonkeeper.fragment.swap.pick_asset.PickAssetType
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.entity.TokenEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class TokenListHelper {

    private val _items = MutableStateFlow(listOf<TokenListItem>())
    private val searchText = MutableStateFlow("")
    val items: Flow<List<TokenListItem>>
        get() = combine(_items, searchText) { items, searchText ->
            when {
                searchText.isBlank() -> items
                else -> {
                    val itemsUpdated = items.asSequence()
                        .filter {
                            it.symbol.contains(
                                searchText,
                                ignoreCase = true
                            ) || it.name.contains(searchText, ignoreCase = true)
                        }
                        .toMutableList()
                    val iterator = itemsUpdated.listIterator()
                    while (iterator.hasNext()) {
                        val current = iterator.next()
                        val pos = ListCell.getPosition(itemsUpdated.size, iterator.previousIndex())
                        if (pos != current.position) {
                            val updatedItem = current.copy(position = pos)
                            iterator.set(updatedItem)
                        }
                    }
                    itemsUpdated
                }
            }
        }.flowOn(Dispatchers.Default)

    suspend fun submitItems(
        domainItems: List<DexAssetBalance>,
        type: PickAssetType,
        toSend: TokenEntity?,
        toReceive: TokenEntity?
    ) = withContext(Dispatchers.Default) {
        val picked = when (type) {
            PickAssetType.SEND -> toSend
            PickAssetType.RECEIVE -> toReceive
        }
        _items.value = domainItems.mapIndexed { index, item ->
            val itemType = when (picked) {
                null -> TokenItemType.NORMAL
                item.tokenEntity -> {
                    when (type) {
                        PickAssetType.SEND -> TokenItemType.TO_SEND
                        PickAssetType.RECEIVE -> TokenItemType.TO_RECEIVE
                    }
                }
                else -> TokenItemType.NORMAL
            }
            TokenListItem(
                model = item,
                iconUri = item.imageUri,
                symbol = item.symbol,
                name = item.displayName,
                position = ListCell.getPosition(domainItems.size, index),
                itemType = itemType
            )
        }
    }

    suspend fun setSearchText(text: String) = withContext(Dispatchers.Default) {
        searchText.value = text
    }
}