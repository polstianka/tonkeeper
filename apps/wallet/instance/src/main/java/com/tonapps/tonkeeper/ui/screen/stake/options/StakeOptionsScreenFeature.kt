package com.tonapps.tonkeeper.ui.screen.stake.options

import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.api.icon
import com.tonapps.tonkeeper.ui.screen.stake.StakeData
import com.tonapps.tonkeeper.ui.screen.stake.StakingRepository
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.TokenRepository
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import core.QueueScope
import io.tonapi.models.PoolImplementationType
import io.tonapi.models.PoolInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uikit.mvi.UiFeature

class StakeOptionsScreenFeature(
    private val tokenRepository: TokenRepository,
    private val stakingRepository: StakingRepository,
    private val settingsRepository: SettingsRepository
) : UiFeature<StakeOptionsScreenState, StakeOptionsScreenEffect>(StakeOptionsScreenState()) {

    private val currency: WalletCurrency
        get() = settingsRepository.currency

    private val currentToken: AccountTokenEntity?
        get() = uiState.value.selectedToken

    private val currentTokenCode: String
        get() = uiState.value.selectedTokenCode

    val currentBalance: Float
        get() = currentToken?.balance?.value?.toFloat() ?: 0f

    val decimals: Int
        get() = currentToken?.decimals ?: 9

    private val queueScope = QueueScope(Dispatchers.IO)

    private fun getCurrentTokenAddress(): String {
        return currentToken?.address ?: "TON"
    }

    fun update(data: StakeData) {
        viewModelScope.launch {
            loadData(data.poolInfo)
        }
    }

    private suspend fun loadData(chosen: PoolInfo?) = withContext(Dispatchers.IO) {
        val wallet = App.walletManager.getWalletInfo()!!
        val accountId = wallet.accountId
        val tokens = tokenRepository.get(currency, accountId, wallet.testnet)
        val stakingData = stakingRepository.pools // todo
        val impls = stakingRepository.implMap

        val optionList = mutableListOf<OptionItem>()

        if (impls.containsKey(PoolImplementationType.liquidTF.value)) {
            optionList.add(OptionItem.Header("Liquid Staking"))
            val liquids = stakingData
                .filter { it.implementation.value == PoolImplementationType.liquidTF.value }
                .sortedByDescending { it.apy }
            optionList.addAll(liquids
                .mapIndexed { index, poolInfo ->
                    OptionItem.Option(
                        index,
                        liquids.size,
                        chosen != null && poolInfo.address == chosen.address,
                        poolInfo
                    )
                })
            optionList.add(OptionItem.Header("Other"))
            var i = 0
            val count = impls.size - 1
            impls.forEach { (key, impl) ->
                if (key != PoolImplementationType.liquidTF.value) {
                    val type = PoolImplementationType.decode(key) ?: PoolImplementationType.liquidTF
                    optionList.add(OptionItem.OptionList(i++, count, type.icon ?: 0, impl, type))
                }
            }
        }

        updateUiState {
            it.copy(
                wallet = wallet,
                tokens = tokens,
                selectedTokenAddress = WalletCurrency.TON.code,
                stakingPools = stakingData,
                selectedPool = chosen,
                optionItems = optionList,
                poolImplementation = null
            )
        }
    }

    fun toLiquidState() {
        viewModelScope.launch {
            loadData(uiState.value.selectedPool)
        }
    }

    fun implChosen(poolImplementationType: PoolImplementationType) {
        val optionList = mutableListOf<OptionItem>()
        val liquids = uiState.value.stakingPools
            .filter { it.implementation.value == poolImplementationType.value }
            .sortedByDescending { it.apy }
        val chosen = uiState.value.selectedPool
        optionList.addAll(liquids
            .mapIndexed { index, poolInfo ->
                OptionItem.Option(
                    index,
                    liquids.size,
                    chosen != null && poolInfo.address == chosen.address,
                    poolInfo
                )
            })
        updateUiState {
            it.copy(
                optionItems = optionList,
                poolImplementation = stakingRepository.implMap[poolImplementationType.value]
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        queueScope.cancel()
    }
}