package com.tonapps.tonkeeper.fragment.swap.tokens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.ui.adapter.Item
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.entity.BalanceEntity
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.rates.entity.RatesEntity
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.stonfi.StonFiRepository
import com.tonapps.wallet.data.stonfi.entities.StonFiTokensEntity
import com.tonapps.wallet.data.token.RawTokensRepository
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.math.BigInteger

class TokenSelectorViewModel(
    rawTokensRepository: RawTokensRepository,
    ratesRepository: RatesRepository,
    walletRepository: WalletRepository,
    settings: SettingsRepository,
    stonFiRepository: StonFiRepository
) : ViewModel() {
    private val assetsFlow =
        stonFiRepository.storageFlow.map { storage -> storage.get(StonFiRepository.Request)?.result }
            .filterNotNull()
    private val tokensFlow = combine(
        rawTokensRepository.storageFlow,
        walletRepository.activeWalletFlow,
        ratesRepository.storageFlow,
        settings.currencyFlow,
        settings.hiddenBalancesFlow
    ) { storage, wallet, rates, currency, hiddenBalance ->
        val ratesEntity = rates.get(RatesRepository.Request(currency, emptyList()))?.result
        storage.get(
            RawTokensRepository.Request(
                accountId = wallet.accountId,
                testnet = wallet.testnet,
                currency = currency
            )
        )?.result?.let { tokens ->
            Tokens2(wallet, currency, tokens.list.filter { it.nano != BigInteger.ZERO }, hiddenBalance, ratesEntity)
        }
    }.filterNotNull()

    private data class Tokens2(
        val wallet: WalletEntity,
        val currency: WalletCurrency,
        val tokens: List<BalanceEntity>,
        val hiddenBalance: Boolean,
        val rates: RatesEntity?
    )

    private val selectedTokenFlow = MutableStateFlow<TokenEntity?>(null)
    private val queryFlow = MutableStateFlow<String?>(null)

    val uiItemsFlow =
        combine(tokensFlow, assetsFlow, selectedTokenFlow, queryFlow, this::buildItems)

    fun search(q: String?) {
        this.queryFlow.value = q
    }

    fun setToken(t: TokenEntity?) {
        selectedTokenFlow.value = t
    }

    var listener: ((t: TokenEntity) -> Unit)? = null

    init {
        viewModelScope.launch {
            stonFiRepository.doRequest()
        }
    }

    /* * */

    private fun buildItems(
        tokens: Tokens2,
        assets: StonFiTokensEntity,
        selected: TokenEntity?,
        query: String?
    ): List<BaseListItem> {
        val uiItems = mutableListOf<BaseListItem>()
        val tokensSet = mutableSetOf<String>()

        val filter =
            selected?.let { token -> assets.list.find { it.token.address == token.address } }?.tokens?.map { it.address }
                ?.toSet()
        val estSize = tokens.tokens.size + assets.list.size

        val needSearch = !query.isNullOrEmpty()
        val needSuggested = !needSearch

        if (needSuggested) {
            val uiSuggestedItems = mutableListOf<Item.TokenSuggested>()
            for (token in tokens.tokens) {
                if (filter != null && !filter.contains(token.token.address)) {
                    continue
                }
                uiSuggestedItems.add(Item.TokenSuggested(
                    iconUri = token.token.imageUri,
                    symbol = token.token.symbol,
                    address = token.token.address,
                    name = token.token.name,
                    onClickListener = { listener?.invoke(token.token) }
                ))
                if (uiSuggestedItems.size > 4) {
                    continue
                }
            }

            if (uiSuggestedItems.size > 0) {
                uiItems.add(Item.TitleLabel1(Localization.suggested))
                uiItems.add(Item.TokenSuggestions(uiSuggestedItems))
                uiItems.add(Item.TitleLabel1(Localization.other))
            }
        }

        var index = 0
        for (balance in tokens.tokens) {
            val token = balance.token

            if (!check(token.name, token.symbol, query)) {
                continue
            }

            if (filter != null && !filter.contains(token.address)) {
                continue
            }

            val fiat = tokens.rates?.convert(token.address, balance.value) ?: 0f
            val balanceFormat = CurrencyFormatter.format(value = balance.value)
            val fiatFormat = CurrencyFormatter.formatFiat(tokens.currency.code, fiat)
            val item = Item.Token(
                position = ListCell.getPosition(estSize, index++),
                iconUri = token.imageUri,
                address = token.address,
                symbol = token.symbol,
                name = token.name,
                balance = balance.value,
                balanceFormat = balanceFormat,
                fiat = fiat,
                fiatFormat = fiatFormat,
                rate = "",
                rateDiff24h = "",
                verified = token.verification == TokenEntity.Verification.whitelist,
                testnet = tokens.wallet.testnet,
                hiddenBalance = tokens.hiddenBalance,
                mode = Item.TokenDisplayMode.SwapSelector,
                onClickListener = { listener?.invoke(token) }
            )

            tokensSet.add(token.address)
            uiItems.add(item)
        }

        for (asset in assets.list) {
            if (filter != null && !filter.contains(asset.token.address)) {
                continue
            }
            if (check(
                    asset.token.name,
                    asset.token.symbol,
                    query
                ) && !tokensSet.contains(asset.token.address)
            ) {
                val balanceFormat = CurrencyFormatter.format(value = 0f)
                val fiatFormat = CurrencyFormatter.formatFiat(tokens.currency.code, 0f)
                uiItems.add(
                    Item.Token(
                        position = ListCell.getPosition(estSize, index++),
                        iconUri = asset.token.imageUri,
                        address = asset.token.address,
                        symbol = asset.token.symbol,
                        name = asset.token.name,
                        verified = true,
                        balance = 0f,
                        balanceFormat = balanceFormat,
                        fiat = 0f,
                        fiatFormat = fiatFormat,
                        hiddenBalance = tokens.hiddenBalance,
                        rate = "", rateDiff24h = "", testnet = tokens.wallet.testnet,
                        mode = Item.TokenDisplayMode.SwapSelector,
                        onClickListener = { listener?.invoke(asset.token) }
                    ))
            }
        }

        if (uiItems.isNotEmpty() && index > 0) {
            val i = uiItems.size - 1
            uiItems[i] = (uiItems[i] as Item.Token).copy(
                position = ListCell.getPosition(
                    index,
                    index - 1
                )
            )
        }

        if (uiItems.isEmpty()) {
            uiItems.add(Item.TitleLabel1(Localization.unknown))
        }

        return uiItems
    }

    private fun check(name: String, symbol: String, query: String?): Boolean {
        return query.isNullOrEmpty() || name.lowercase().startsWith(query) || symbol.lowercase()
            .startsWith(query)
    }
}