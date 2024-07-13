package com.tonapps.tonkeeper.ui.screen.swap.choose

import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.api.swap.StonfiSwapAsset
import com.tonapps.tonkeeper.api.swap.SwapRepository
import com.tonapps.tonkeeper.ui.screen.swap.SwapData
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.TokenRepository
import core.QueueScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uikit.mvi.UiFeature

class SwapChooseScreenFeature(
    private val tokenRepository: TokenRepository,
    private val swapRepository: SwapRepository,
    private val settingsRepository: SettingsRepository
) : UiFeature<SwapChooseScreenState, SwapChooseScreenEffect>(SwapChooseScreenState()) {

    private val currency: WalletCurrency
        get() = settingsRepository.currency

    private val queueScope = QueueScope(Dispatchers.IO)

    fun update(data: SwapData) {
        viewModelScope.launch {
            loadData(if (data.currentFrom) data.to else data.from)
        }
    }

    private suspend fun loadData(oppositeAsset: StonfiSwapAsset?) = withContext(Dispatchers.IO) {
        val wallet = App.walletManager.getWalletInfo()!!
        val accountId = wallet.accountId
        val tokens = tokenRepository.get(currency, accountId, wallet.testnet)

        val assets = if (oppositeAsset == null) {
            swapRepository.getAssets(wallet.address)
        } else {
            swapRepository.getPairedAssets(oppositeAsset) ?: swapRepository.getAssets(wallet.address)
        }.sortedByDescending { it.balance.toFloat() }
        val suggestedAddresses = swapRepository.getSuggestedAddresses(wallet.address)
        val suggested = mutableListOf<StonfiSwapAsset>()
        suggested.addAll(assets.filter { it.contractAddress in suggestedAddresses })

        updateUiState {
            it.copy(
                wallet = wallet,
                tokens = tokens,
                selectedTokenAddress = WalletCurrency.TON.code,
                assets = assets,
                filteredAssets = assets,
                suggestedAssets = suggested,
                chosen = null
            )
        }
    }

    fun setSearchQuery(query: String) {
        queueScope.submit(Dispatchers.IO) {
            val assets = uiState.value.assets.filter {
                it.toString().lowercase().contains(query.lowercase())
            }.sortedByDescending { it.balance.toFloat() }
            updateUiState { state ->
                state.copy(
                    filteredAssets = assets
                )
            }
        }
    }

    fun selectAsset(add: StonfiSwapAsset) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val current =
                    uiState.value.suggestedAssets.map { it.contractAddress }.toMutableList()
                if (!current.contains(add.contractAddress)) {
                    if (current.size == 3) {
                        current.removeAt(2)
                    }
                    current.add(0, add.contractAddress)
                    uiState.value.wallet?.let { wallet ->
                        swapRepository.addSuggested(wallet.address, current)
                    }
                }

                val suggested = mutableListOf<StonfiSwapAsset>()
                suggested.addAll(uiState.value.assets.filter { it.contractAddress in current })

                updateUiState { currentState ->
                    currentState.copy(
                        chosen = add,
                        suggestedAssets = suggested
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        queueScope.cancel()
    }
}