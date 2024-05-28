package com.tonapps.tonkeeper.ui.screen.swapnative.choose

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.network.NetworkMonitor
import com.tonapps.tonkeeper.ui.screen.swapnative.choose.list.TokenTypeItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.AssetRepository
import com.tonapps.wallet.data.token.TokenRepository
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import com.tonapps.wallet.data.token.entities.AssetEntity
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

    private val _uiItemListFlow = MutableStateFlow<List<TokenTypeItem>>(emptyList())
    val uiItemListFlow = _uiItemListFlow.asStateFlow()

    init {

        combine(
            walletRepository.activeWalletFlow,
            settings.currencyFlow,
            networkMonitor.isOnlineFlow
        ) { wallet, currency, isOnline ->

            _tokenListFlow.value =
                tokenRepository.getLocal(currency, wallet.accountId, wallet.testnet)

        }.launchIn(viewModelScope)

        _symbolToAssetMapFlow.combine(_tokenListFlow) { assetMap, tokenList ->

            tokenList.forEach { token ->
                assetMap[token.symbol]?.balance = token.balance.value
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

        Log.d("search-asset", "populateList: ${searchQuery}, ${assetMap}, ${sortedAssetList}")

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
                Log.d("asset-get", "getRemoteAssets: ${assets.toString()}")

            } catch (e: Throwable) {
                _symbolToAssetMapFlow.value = emptyMap()
                Log.d("asset-get", "getRemoteAssets: ${e.message}")
            }
        }


    fun selectSellToken(contractAddress: String) {
        assetRepository.setSelectedSellToken(contractAddress)
    }

    private fun generateList(assetList: List<AssetEntity>): List<TokenTypeItem> {
        val hiddenBalance = settings.hiddenBalances

        return assetList.mapIndexed { index, assetEntity ->

            val balanceFormat = CurrencyFormatter.format(value = assetEntity.balance)

            TokenTypeItem(
                assetEntity.imageUrl?.toUri(),
                assetEntity.contractAddress,
                assetEntity.displayName ?: "",
                assetEntity.symbol,
                assetEntity.balance,
                balanceFormat,
                hiddenBalance,
                false,
                ListCell.getPosition(assetList.size, index)
            )

        }
    }

}