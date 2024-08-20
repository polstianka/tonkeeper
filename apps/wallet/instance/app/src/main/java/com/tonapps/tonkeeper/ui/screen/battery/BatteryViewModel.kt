package com.tonapps.tonkeeper.ui.screen.battery

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.ui.screen.battery.list.Item
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.AccountRepository
import com.tonapps.wallet.data.battery.BatteryRepository
import com.tonapps.wallet.data.battery.entity.BatterySupportedTransaction
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.TokenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take

class BatteryViewModel(
    app: Application,
    private val accountRepository: AccountRepository,
    private val batteryRepository: BatteryRepository,
    private val tokenRepository: TokenRepository,
    private val settingsRepository: SettingsRepository,
    api: API,
) : AndroidViewModel(app) {
    private val _uiItemsFlow = MutableStateFlow<List<Item>?>(null)
    val uiItemsFlow = _uiItemsFlow.asStateFlow().filterNotNull()

    private val _uiSettingsItemsFlow = MutableStateFlow<List<Item>?>(null)
    val uiSettingsItemsFlow = _uiSettingsItemsFlow.asStateFlow().filterNotNull()

    private val _configFlow =
        accountRepository.selectedWalletFlow.map { wallet ->
            State.Config(wallet, batteryRepository.getConfig(wallet.testnet))
        }.flowOn(Dispatchers.IO)

    private val _batteryFlow = combine(
        _configFlow, batteryRepository.balanceFlow
    ) { configState, _ ->
        val balance = batteryRepository.getBalance(configState.wallet).balance
        val charges =
            BatteryRepository.convertToCharges(balance, api.config.batteryMeanFees)
        State.Battery(
            balance = balance.value.toFloat(),
            charges = charges,
            isBeta = api.config.batteryBeta,
        )
    }.flowOn(Dispatchers.IO)


    private val _settingsFlow = MutableStateFlow<State.Settings?>(null)
    val settingsFlow = _settingsFlow.asStateFlow().filterNotNull()

    private val _rechargeMethodsFlow = _configFlow.map { configState ->
        val tokens = tokenRepository.getLocal(
            settingsRepository.currency, configState.wallet.accountId, configState.wallet.testnet
        )
        State.RechargeMethods(
            batteryConfig = configState.batteryConfig,
            tokens = tokens,
            disabled = api.config.disableBatteryCryptoRecharge,
        )
    }.flowOn(Dispatchers.IO)

    init {
        _configFlow.map { configState ->
            State.Settings(
                batteryRepository.getSupportedTransactions(configState.wallet.accountId)
            )
        }.onEach {
            _settingsFlow.tryEmit(it)
        }.flowOn(Dispatchers.IO).take(1).launchIn(viewModelScope)

        combine(
            _batteryFlow, settingsFlow, _rechargeMethodsFlow
        ) { battery, settings, rechargeMethods ->
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
                )
            )
        }.launchIn(viewModelScope)
    }

    fun setSupportedTransaction(transaction: BatterySupportedTransaction, enabled: Boolean) {
        accountRepository.selectedWalletFlow.onEach { wallet ->
            val supportedTransactions =
                batteryRepository.setSupportedTransaction(wallet.accountId, transaction, enabled)
            _settingsFlow.tryEmit(State.Settings(supportedTransactions))
        }.take(1).launchIn(viewModelScope)
    }
}