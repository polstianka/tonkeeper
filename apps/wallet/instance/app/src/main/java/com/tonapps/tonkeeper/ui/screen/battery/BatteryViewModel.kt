package com.tonapps.tonkeeper.ui.screen.battery

import android.app.Application
import com.tonapps.extensions.MutableEffectFlow
import com.tonapps.tonkeeper.ui.base.BaseWalletVM
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.AccountRepository
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.battery.BatteryMapper
import com.tonapps.wallet.data.battery.BatteryRepository
import com.tonapps.wallet.data.battery.entity.BatteryBalanceEntity
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.TokenRepository
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

class BatteryViewModel(
    app: Application
) : BaseWalletVM(app) {

    private val _routeFlow = MutableEffectFlow<BatteryRoute>() // with "_" writable only inside
    val routeFlow = _routeFlow.asSharedFlow().filterNotNull() // without "_" read-only for outside

    private val _titleFlow = MutableStateFlow<CharSequence?>(null)
    val titleFlow = _titleFlow.asStateFlow()

    init {
        routeToRefill()
    }

    fun routeToSettings() {
        _routeFlow.tryEmit(BatteryRoute.Settings)
    }

    private fun routeToRefill() {
        _routeFlow.tryEmit(BatteryRoute.Refill)
    }

    fun setTitle(title: CharSequence?) {
        _titleFlow.value = title
    }
}