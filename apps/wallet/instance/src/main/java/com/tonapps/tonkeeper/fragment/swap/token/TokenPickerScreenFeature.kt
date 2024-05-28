package com.tonapps.tonkeeper.fragment.swap.token

import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.api.jettonPreview
import com.tonapps.tonkeeper.fragment.swap.model.TokenInfo
import com.tonapps.tonkeeper.fragment.swap.token.list.TokenItem
import com.tonapps.tonkeeper.fragment.swap.token.suggestions.SuggestionTokenItem
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.events.EventsRepository
import io.tonapi.models.Action
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import ton.TonAddress

class TokenPickerScreenFeature(
    private val walletRepository: WalletRepository,
    private val eventsRepository: EventsRepository,
) : ViewModel() {

    private val _searchInput = MutableStateFlow("")

    private val _tokensFlow = MutableStateFlow<List<TokenItem>>(emptyList())
    val tokensFlow = _tokensFlow.combine(_searchInput) { items, search ->
        if (search.isEmpty()) {
            items
        } else {
            items.filter { item ->
                item.tokenInfo.name.contains(search, true) || item.tokenInfo.symbol.contains(
                    search,
                    true
                )
            }
        }
    }

    val suggestionTokens = walletRepository.activeWalletFlow
        .map { wallet ->
            eventsRepository.getLocal(wallet.accountId, wallet.testnet)?.events ?: emptyList()
        }
        .map { accountEvents ->
            accountEvents.asSequence().filter { event ->
                event.actions.any { action ->
                    action.type == Action.Type.jettonSwap
                }
            }.flatMap { event -> event.actions }
                .mapNotNull { action -> action.jettonSwap?.jettonPreview?.address }
                .distinct()
                .take(SUGGESTIONS_COUNT)
                .toList()
        }
        .map { list ->
            list.ifEmpty {
                _tokensFlow.value.sortedBy { it.tokenInfo.balanceFiat }.take(SUGGESTIONS_COUNT)
                    .map { TonAddress(it.tokenInfo.contractAddress).raw() }.ifEmpty {
                    _tokensFlow.value.shuffled().take(SUGGESTIONS_COUNT)
                        .map { TonAddress(it.tokenInfo.contractAddress).raw() }
                }
            }
        }
        .combine(_tokensFlow) { tokenAddressList, tokens ->
            tokens.filter { token ->
                tokenAddressList.any { addr ->
                    addr.equals(TonAddress(token.tokenInfo.contractAddress).raw(), true)
                }
            }
        }
        .map { list -> list.map { item -> SuggestionTokenItem(item.tokenInfo) } }
        .flowOn(Dispatchers.IO)

    fun setupTokens(tokens: Array<TokenInfo>, selectedToken: TokenInfo?, exceptToken: TokenInfo?) {
        _tokensFlow.update {
            tokens
                .filterNot { it == exceptToken }
                .mapIndexed { index, token ->
                    val position = com.tonapps.uikit.list.ListCell.getPosition(tokens.size, index)
                    TokenItem(
                        tokenInfo = token,
                        position = position,
                        selected = token == selectedToken
                    )
                }
        }
    }

    fun search(newValue: String) {
        _searchInput.update { newValue }
    }

    private companion object {
        private const val SUGGESTIONS_COUNT = 2
    }
}