package com.tonapps.tonkeeper.ui.screen.stake

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.Coin
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.api.getAddress
import com.tonapps.tonkeeper.api.jetton.JettonRepository
import com.tonapps.tonkeeper.api.parsedBalance
import com.tonapps.tonkeeper.api.withRetry
import com.tonapps.tonkeeper.core.history.HistoryHelper
import com.tonapps.tonkeeper.core.history.list.item.HistoryItem
import com.tonapps.tonkeeper.fragment.jetton.list.JettonItem
import com.tonapps.tonkeeper.helper.NumberFormatter
import com.tonapps.tonkeeper.ui.screen.stake.StakedJettonState.Companion.getItems
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.WalletType
import com.tonapps.wallet.data.account.legacy.WalletLegacy
import com.tonapps.wallet.data.account.legacy.WalletManager
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.stake.StakeRepository
import com.tonapps.wallet.localization.Localization
import core.ResourceManager
import io.tonapi.models.AccountEvents
import io.tonapi.models.JettonBalance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uikit.mvi.AsyncState

class StakedJettonViewModel(
    private val historyHelper: HistoryHelper,
    private val ratesRepository: RatesRepository,
    private val walletManager: WalletManager,
    private val jettonRepository: JettonRepository,
    private val settingsRepository: SettingsRepository,
    private val stakeRepository: StakeRepository,
    private val resourceManager: ResourceManager,
    private val api: API
) : ViewModel() {

    private val _uiState = MutableStateFlow(StakedJettonState())
    val uiState: StateFlow<StakedJettonState> = _uiState

    fun load(address: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val wallet = walletManager.getWalletInfo() ?: error("Wallet not found")
            val accountId = wallet.accountId
            val jetton = jettonRepository.getByAddress(accountId, address, wallet.testnet)
                ?: throw IllegalStateException("Jetton not found")
            val currency = settingsRepository.currency
            val balance = jetton.parsedBalance
            val jettonAddress = jetton.getAddress(wallet.testnet)

            val rates = ratesRepository.getRates(currency, address)

            val currencyBalance = rates.convert(address, balance)
            val historyItems = getEvents(wallet, address)

            val stakePoolsEntity = stakeRepository.get()
            val pools = stakePoolsEntity.pools
            val maxApy = pools.maxByOrNull { it.apy } ?: error("No pools")
            val pool = if (address.isEmpty()) maxApy else pools.first {
                it.address == address || it.liquidJettonMaster == address
            }
            val isMaxApy = address.isEmpty() || pool.address == maxApy.address
            val implementation =
                stakePoolsEntity.implementations.getValue(pool.implementation.value)
            val links = implementation.socials + implementation.url

            _uiState.update {
                it.copy(
                    walletAddress = wallet.address,
                    walletType = wallet.type,
                    asyncState = AsyncState.Default,
                    jetton = jetton,
                    currencyBalance = CurrencyFormatter.formatFiat(currency.code, currencyBalance),
                    historyItems = historyItems,
                    isMaxApy = isMaxApy,
                    apy = resourceManager.getString(
                        Localization.apy_short_percent_placeholder,
                        NumberFormatter.format(pool.apy)
                    ),
                    minDeposit = "${NumberFormatter.format(Coin.toCoins(pool.minStake))} TON",
                    links = links
                )
            }
            _uiState.update {
                it.copy(items = it.getItems(resourceManager))
            }
        }
    }

    private suspend fun getEvents(
        wallet: WalletLegacy,
        jettonAddress: String,
        beforeLt: Long? = null
    ): List<HistoryItem> = withContext(Dispatchers.IO) {
        val accountId = wallet.accountId
        val events = getAccountEvent(accountId, wallet.testnet, jettonAddress, beforeLt)
            ?: return@withContext emptyList()
        historyHelper.mapping(wallet, events)
    }

    private suspend fun getAccountEvent(
        accountId: String,
        testnet: Boolean,
        jettonAddress: String,
        beforeLt: Long? = null
    ): AccountEvents? {
        return withRetry {
            api.accounts(testnet).getAccountJettonHistoryByID(
                accountId = accountId,
                jettonId = jettonAddress,
                beforeLt = beforeLt,
                limit = HistoryHelper.EVENT_LIMIT
            )
        }
    }

}

data class StakedJettonState(
    val walletAddress: String = "",
    val walletType: WalletType = WalletType.Default,
    val asyncState: AsyncState = AsyncState.Loading,
    val jetton: JettonBalance? = null,
    val currencyBalance: CharSequence = "",
    val historyItems: List<HistoryItem> = emptyList(),
    val isMaxApy: Boolean = false,
    val apy: String = "",
    val minDeposit: String = "",
    val links: List<String> = emptyList(),
    val items: List<JettonItem> = emptyList()
) {
    val balance: CharSequence
        get() {
            val jetton = jetton ?: return ""
            return CurrencyFormatter.format(
                jetton.jetton.symbol,
                jetton.parsedBalance,
                jetton.jetton.decimals
            )
        }

    companion object {
        fun StakedJettonState.getItems(resourceManager: ResourceManager): List<JettonItem> {
            val jetton = jetton ?: return emptyList()
            val items = mutableListOf<JettonItem>()
            items.add(
                JettonItem.Header(
                    balance = balance,
                    currencyBalance = currencyBalance,
                    iconUrl = jetton.jetton.image,
                    rate = null,
                    diff24h = null,
                    staked = true
                )
            )
            items.add(JettonItem.ActionsStaked(walletAddress, jetton, walletType))
            items.add(JettonItem.Description(resourceManager.getString(Localization.staked_jetton_description)))
            items.add(JettonItem.Details(isApyMax = isMaxApy, apy = apy, minDeposit = minDeposit))
            items.add(JettonItem.Description(resourceManager.getString(Localization.pool_details_disclaimer)))
            items.add(JettonItem.Links(links))
            return items
        }
    }

}