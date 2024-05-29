package com.tonapps.tonkeeper.ui.screen.swap.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.extensions.MutableEffectFlow
import com.tonapps.tonkeeper.ui.screen.swap.assets.list.EmptyItem
import com.tonapps.tonkeeper.ui.screen.swap.assets.list.HeaderItem
import com.tonapps.tonkeeper.ui.screen.swap.assets.list.Item
import com.tonapps.tonkeeper.ui.screen.swap.assets.list.SuggestedTokensItem
import com.tonapps.tonkeeper.ui.screen.swap.assets.list.TokenItem
import com.tonapps.tonkeeper.ui.screen.swap.assets.list.TokenPatchItem
import com.tonapps.tonkeeper.ui.screen.swap.data.AssetEntity
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map

class AssetPickerViewModel(
    private val settings: SettingsRepository,
    private val args: AssetPickerArgs
): ViewModel() {
    companion object {
        private fun AssetEntity.matchesQuery(query: String, regex: Regex): Boolean {
            return this.token.symbol.contains(regex) || this.token.name.contains(regex)
        }
    }

    private val _queryFlow = MutableEffectFlow<String>()

    private val _uiItemsFlow = MutableStateFlow(buildUiItems())
    val uiItemsFlow: Flow<List<Item>> = _uiItemsFlow.asStateFlow().filter { it.isNotEmpty() }

    @OptIn(FlowPreview::class)
    val queryFlow = _queryFlow.asSharedFlow()
        .map { it.trim() }
        .debounce { if (it.isEmpty()) 0L else 30L }
        .map {
            val items = buildUiItems(it)
            _uiItemsFlow.tryEmit(items)
        }
        .flowOn(Dispatchers.IO)

    fun query(query: String) {
        _queryFlow.tryEmit(query)
    }

    init {
        queryFlow.launchIn(viewModelScope)
    }

    private fun filter(list: List<AssetEntity>, query: String = ""): List<AssetEntity> {
        if (query.isNotBlank()) {
            val regex = Regex("(?<=^|\\s)${Regex.escape(query)}", RegexOption.IGNORE_CASE)
            return list.filter {
                if (args.limitToMarkets != null && !args.limitToMarkets.contains(it.userFriendlyAddress)) {
                    false
                } else {
                    it.matchesQuery(query, regex)
                }
            }
        } else if (args.limitToMarkets != null) {
            return list.filter {
                args.limitToMarkets.contains(it.userFriendlyAddress) ||
                (args.selectedToken != null && it.token.address == args.selectedToken.address)
            }
        }
        return list
    }

    private fun buildUiItems(searchQuery: String = ""): List<Item> {
        val items = mutableListOf<Item>()

        if (searchQuery.isEmpty()) {
            filter(args.remoteAssets.suggestedList, searchQuery).let {
                val suggestedTokensList = it.mapIndexed { index, assetEntity ->
                    val position = when (index) {
                        0 -> if (it.size == 1) {
                            ListCell.Position.SINGLE
                        } else {
                            ListCell.Position.FIRST
                        }

                        it.size - 1 -> ListCell.Position.LAST
                        else -> ListCell.Position.MIDDLE
                    }
                    TokenPatchItem(assetEntity, position)
                }
                if (suggestedTokensList.isNotEmpty()) {
                    items.add(HeaderItem(Localization.assets_suggested))
                    items.add(SuggestedTokensItem(suggestedTokensList))
                }
            }
        }
        filter(args.remoteAssets.displayList, searchQuery).let{
            val tokenItems = it.mapIndexed { index, assetEntity ->
                val position = when (index) {
                    0 -> if (it.size == 1) {
                        ListCell.Position.SINGLE
                    } else {
                        ListCell.Position.FIRST
                    }
                    it.size - 1 -> ListCell.Position.LAST
                    else -> ListCell.Position.MIDDLE
                }
                TokenItem(assetEntity, position)
            }
            if (tokenItems.isNotEmpty()) {
                if (items.isNotEmpty()) {
                    items.add(HeaderItem(Localization.assets_other))
                }
                items.addAll(tokenItems)
            }
        }
        if (items.isEmpty()) {
            items.add(EmptyItem(if (searchQuery.isEmpty()) Localization.swap_market_unavailable else Localization.swap_search_empty))
        }
        return items
    }


}