package com.tonapps.tonkeeper.ui.screen.battery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.ui.base.BaseWalletVM
import com.tonapps.tonkeeper.ui.screen.battery.list.Item
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.AccountRepository
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.battery.BatteryMapper
import com.tonapps.wallet.data.battery.BatteryRepository
import com.tonapps.wallet.data.battery.entity.BatteryBalanceEntity
import com.tonapps.wallet.data.settings.BatteryTransaction
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.TokenRepository
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import uikit.extensions.collectFlow

class BatteryViewModel(
    app: Application,
    private val accountRepository: AccountRepository,
    private val batteryRepository: BatteryRepository,
    private val tokenRepository: TokenRepository,
    private val settingsRepository: SettingsRepository,
    private val api: API,
) : BaseWalletVM(app) {

    private val _uiItemsFlow = MutableStateFlow<List<Item>?>(null)
    val uiItemsFlow = _uiItemsFlow.asStateFlow().filterNotNull()

    private val _uiSettingsItemsFlow = MutableStateFlow<List<Item>?>(null)
    val uiSettingsItemsFlow = _uiSettingsItemsFlow.asStateFlow().filterNotNull()

    private val configFlow = accountRepository.selectedWalletFlow.map { wallet ->
        State.Config(wallet, batteryRepository.getConfig(wallet.testnet))
    }

    init {
        collectFlow(configFlow.flowOn(Dispatchers.IO)) { config ->
            val settings = State.Settings(settingsRepository.getBatteryTxEnabled(config.wallet.accountId))
            val battery = createBattery(config)
            val rechargeMethods = createRechargeMethods(config)

            val uiItems = mutableListOf<Item>()
            uiItems.add(battery.uiItem())
            if (battery.balance > 0) {
                uiItems.add(settings.uiItem())
                uiItems.add(Item.Space())
            }
            uiItems.addAll(rechargeMethods.uiItems())
            uiItems.add(Item.Space())
            _uiItemsFlow.tryEmit(uiItems.toList())
            _uiSettingsItemsFlow.tryEmit(
                settings.uiItems(
                    hasBalance = battery.balance > 0,
                    config = api.config,
                    wallet = config.wallet,
                )
            )
        }
    }

    private suspend fun createRechargeMethods(config: State.Config) = State.RechargeMethods(
        batteryConfig = config.batteryConfig,
        tokens = getTokens(config.wallet),
        disabled = api.config.disableBatteryCryptoRecharge,
    )

    private suspend fun createBattery(config: State.Config): State.Battery {
        val balance = getBalance(config.wallet).balance
        val charges = BatteryMapper.convertToCharges(balance, api.config.batteryMeanFees)
        return State.Battery(
            balance = balance.value.toFloat(),
            charges = charges,
            isBeta = api.config.batteryBeta,
        )
    }

    private suspend fun getTokens(wallet: WalletEntity): List<AccountTokenEntity> {
        return tokenRepository.get(
            currency = settingsRepository.currency,
            accountId = wallet.accountId,
            testnet = wallet.testnet
        ) ?: emptyList()
    }

    private suspend fun getBalance(wallet: WalletEntity): BatteryBalanceEntity {
        val tonProofToken = accountRepository.requestTonProofToken(wallet) ?: return BatteryBalanceEntity.Empty
        return batteryRepository.getBalance(
            tonProofToken = tonProofToken,
            publicKey = wallet.publicKey,
            testnet = wallet.testnet,
        )
    }
}