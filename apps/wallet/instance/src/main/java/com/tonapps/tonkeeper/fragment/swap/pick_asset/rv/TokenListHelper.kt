package com.tonapps.tonkeeper.fragment.swap.pick_asset.rv

import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAsset
import com.tonapps.uikit.list.ListCell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.withContext

class TokenListHelper {

    private val _items = MutableStateFlow(listOf<TokenListItem>())
    private val searchText = MutableStateFlow("")
    val items: Flow<List<TokenListItem>>
        get() = combine(_items, searchText) { items, searchText ->
            when {
                searchText.isBlank() -> items
                else -> {
                    val itemsUpdated = items.filter {
                            it.symbol.contains(
                                searchText,
                                ignoreCase = true
                            ) || it.name.contains(searchText, ignoreCase = true)
                        }
                    itemsUpdated.mapIndexed { index, item ->
                        item.copy(position = ListCell.getPosition(itemsUpdated.size, index))
                    }
                }
            }
        }

    suspend fun submitItems(domainItems: List<DexAsset>) = withContext(Dispatchers.Default) {
        _items.value = domainItems.mapIndexed { index, item ->
            TokenListItem(
                model = item,
                iconUrl = item.imageUri,
                symbol = item.symbol,
                amountCrypto = "",
                name = item.displayName,
                amountFiat = "",
                amountCryptoColor = com.tonapps.uikit.color.R.attr.textPrimaryColor,
                position = ListCell.getPosition(domainItems.size, index)
            )
        }
    }

    suspend fun setSearchText(text: String) = withContext(Dispatchers.Default) {
        searchText.value = text
    }

}