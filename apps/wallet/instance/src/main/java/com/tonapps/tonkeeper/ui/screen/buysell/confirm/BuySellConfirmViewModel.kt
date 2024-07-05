package com.tonapps.tonkeeper.ui.screen.buysell.confirm

import androidx.lifecycle.ViewModel
import com.tonapps.network.NetworkMonitor
import com.tonapps.tonkeeper.core.fiat.models.FiatItem
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.rates.entity.OperatorBuyRateEntity
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.TokenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BuySellConfirmViewModel(
    private val networkMonitor: NetworkMonitor,
    private val walletRepository: WalletRepository,
    private val settings: SettingsRepository,
    private val tokenRepository: TokenRepository,
    private val api: API,
) : ViewModel() {

    private val _screenStateFlow =
        MutableStateFlow<BuySellConfirmScreenState>(BuySellConfirmScreenState.initState)
    val screenStateFlow: StateFlow<BuySellConfirmScreenState> = _screenStateFlow

//    val amount: Double = 0.0
//    lateinit var operatorBuyRate: OperatorBuyRateEntity
//    lateinit var fiatItem: FiatItem

    init {

    }


}