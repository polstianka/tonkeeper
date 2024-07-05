package com.tonapps.tonkeeper.ui.screen.swapnative.choose

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.network.NetworkMonitor
import com.tonapps.tonkeeper.ui.screen.swapnative.choose.list.Item
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.AssetRepository
import com.tonapps.wallet.data.token.TokenRepository
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import com.tonapps.wallet.data.token.entities.AssetEntity
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChooseTokenViewModel(
    private val assetRepository: AssetRepository,
    private val networkMonitor: NetworkMonitor,
    private val walletRepository: WalletRepository,
    private val settings: SettingsRepository,
    private val tokenRepository: TokenRepository,
) : ViewModel() {

    private val _symbolToAssetMapFlow = MutableStateFlow<Map<String, AssetEntity>>(emptyMap())
    private val _tokenListFlow = MutableStateFlow<List<AccountTokenEntity>>(emptyList())

    private val _uiItemListFlow = MutableStateFlow<List<Item>>(emptyList())
    val uiItemListFlow = _uiItemListFlow.asStateFlow()

    var selectedCurrency: WalletCurrency? = null

    init {

        combine(
            walletRepository.activeWalletFlow,
            settings.currencyFlow,
            networkMonitor.isOnlineFlow
        ) { wallet, currency, isOnline ->

            selectedCurrency = currency

            _tokenListFlow.value =
                tokenRepository.getLocal(currency, wallet.accountId, wallet.testnet)

        }.launchIn(viewModelScope)

        _symbolToAssetMapFlow.combine(_tokenListFlow) { assetMap, tokenList ->

            tokenList.forEach { token ->
                assetMap.get(token.symbol)?.also {
                    it.balance = token.balance.value
                    it.rate = token.rateNow
                }
                // assetMap[token.symbol]?.balance = token.balance.value
            }

            populateList()
        }.launchIn(viewModelScope)

    }

    fun populateList(searchQuery: String? = null) {
        // search
        val assetMap = if (searchQuery.isNullOrEmpty()) _symbolToAssetMapFlow.value
        else _symbolToAssetMapFlow.value.filterKeys { key ->
            key.toLowerCase().contains(searchQuery)
        }

        // generate ui list
        val sortedAssetList = assetMap.values.toList()
            .sortedWith(compareBy<AssetEntity> {
                !it.isTon
            }.thenByDescending {
                it.balance
            }.thenBy { it.symbol.lowercase() })

        _uiItemListFlow.value = generateList(sortedAssetList)
    }

    fun getSellAssets() {
        viewModelScope.launch {
            getRemoteAssets()
        }
    }

    fun getBuyAssets(contractAddress: String) {
        viewModelScope.launch {
            getRemoteAssets(contractAddress)
        }
    }

    private suspend fun getRemoteAssets(contractAddress: String? = null): Unit =
        withContext(Dispatchers.IO) {
            try {
                val allAssets = assetRepository.get(false)
                val assets = if (contractAddress.isNullOrEmpty()) {
                    allAssets.values.toList().associateBy { it.symbol }.toMutableMap()
                } else {
                    allAssets[contractAddress]?.swapableAssets?.mapNotNull {
                        allAssets[it]
                    }?.associateBy { it.symbol }?.toMutableMap() ?: emptyMap()
                }

                _symbolToAssetMapFlow.value = assets

            } catch (e: Throwable) {
                _symbolToAssetMapFlow.value = emptyMap()
            }
        }


    fun selectSellToken(contractAddress: String) {
        assetRepository.setSelectedSellToken(contractAddress)
    }

    private fun generateList(assetList: List<AssetEntity>): List<Item> {
        val hiddenBalance = settings.hiddenBalances

        val SuggestedTitleItem = Item.Title(Localization.suggested, ListCell.Position.SINGLE)
        val OtherTitleItem = Item.Title(Localization.other, ListCell.Position.SINGLE)

        val suggestedItemList = assetList.filter { listOf("USD₮").contains(it.symbol) }
            .mapIndexed { index, assetEntity ->
                createItemSuggested(assetEntity, hiddenBalance)
            }

        val tokenList = assetList.mapIndexed { index, assetEntity ->
            createItemTypeToken(
                assetEntity,
                hiddenBalance,
                ListCell.getPosition(assetList.size, index)
            )
        }

        val suggestedList = if(suggestedItemList.isEmpty()) emptyList() else listOf(SuggestedTitleItem) + suggestedItemList + listOf(OtherTitleItem)

        return suggestedList + tokenList
    }

    private fun createItemTypeToken(
        assetEntity: AssetEntity,
        hiddenBalance: Boolean,
        position: ListCell.Position
    ): Item.TokenType {
        val balanceFormat = CurrencyFormatter.format(value = assetEntity.balance)
        var fiatBalance = (assetEntity.rate * assetEntity.balance).toString()
        if (selectedCurrency != null) {
            fiatBalance = CurrencyFormatter.format(
                selectedCurrency?.code!!,
                (assetEntity.rate * assetEntity.balance)
            ).toString()
        }

        return Item.TokenType(
            assetEntity.imageUrl?.toUri(),
            assetEntity.contractAddress,
            assetEntity.displayName ?: "",
            assetEntity.symbol,
            assetEntity.balance,
            fiatBalance,
            assetEntity.rate,
            balanceFormat,
            hiddenBalance,
            false,
            position
        )
    }

    private fun createItemSuggested(
        assetEntity: AssetEntity,
        hiddenBalance: Boolean
    ): Item.Suggested {
        val balanceFormat = CurrencyFormatter.format(value = assetEntity.balance)
        var fiatBalance = (assetEntity.rate * assetEntity.balance).toString()
        if (selectedCurrency != null) {
            fiatBalance = CurrencyFormatter.format(
                selectedCurrency?.code!!,
                (assetEntity.rate * assetEntity.balance)
            ).toString()
        }

        return Item.Suggested(
            assetEntity.imageUrl?.toUri(),
            assetEntity.contractAddress,
            assetEntity.displayName ?: "",
            assetEntity.symbol,
            assetEntity.balance,
            fiatBalance,
            assetEntity.rate,
            balanceFormat,
            hiddenBalance,
            false,
            ListCell.Position.SINGLE
        )
    }

}