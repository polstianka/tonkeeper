package com.tonapps.tonkeeper.fragment.swap.pick_asset.rv

import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAsset
import com.tonapps.uikit.list.ListCell
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter

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

    fun submitItems(domainItems: List<DexAsset>) {
        _items.value = domainItems.mapIndexed { index, item ->
            TokenListItem(
                model = item,
                iconUrl = item.imageUrl,
                symbol = item.symbol,
                amountCrypto = "",
                name = item.displayName,
                amountFiat = "",
                amountCryptoColor = com.tonapps.uikit.color.R.attr.textPrimaryColor,
                position = ListCell.getPosition(domainItems.size, index)
            )
        }
    }

    fun setSearchText(text: String) {
        searchText.value = text
    }

}