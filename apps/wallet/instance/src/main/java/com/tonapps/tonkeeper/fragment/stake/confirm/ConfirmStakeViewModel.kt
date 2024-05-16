package com.tonapps.tonkeeper.fragment.stake.confirm

import androidx.lifecycle.ViewModel
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.core.emit
import com.tonapps.tonkeeper.extensions.formattedRate
import com.tonapps.tonkeeper.fragment.stake.confirm.rv.ConfirmStakeItemType
import com.tonapps.tonkeeper.fragment.stake.domain.getOperationStringResId
import com.tonapps.tonkeeper.fragment.stake.presentation.getIconDrawableRes
import com.tonapps.tonkeeper.fragment.trade.domain.GetRateFlowCase
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.account.legacy.WalletManager
import com.tonapps.wallet.data.settings.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class ConfirmStakeViewModel(
    settingsRepository: SettingsRepository,
    getRateFlowCase: GetRateFlowCase,
    walletRepository: WalletRepository,
    mapper: ConfirmStakeListItemMapper
) : ViewModel() {
    companion object {
        private const val TOKEN_TON = "TON"
    }

    private val exchangeRate = settingsRepository.currencyFlow
        .flatMapLatest { getRateFlowCase.execute(it) }
    private val args = MutableSharedFlow<ConfirmStakeArgs>(replay = 1)
    private val _events = MutableSharedFlow<ConfirmStakeEvent>()

    val events: Flow<ConfirmStakeEvent>
        get() = _events
    val icon = args.map { it.pool.serviceType.getIconDrawableRes() }
    val operationText = args.map { it.type.getOperationStringResId() }
    val amountCryptoText = args.map { CurrencyFormatter.format(TOKEN_TON, it.amount) }
    val amountFiatText = formattedRate(exchangeRate, args.map { it.amount }, TOKEN_TON)
    val items = combine(args, walletRepository.activeWalletFlow) { args, wallet ->
        ConfirmStakeItemType.entries.map { mapper.map(it, wallet, args.pool) }
    }

    fun provideArgs(args: ConfirmStakeArgs) {
        emit(this.args, args)
    }

    fun onChevronClicked() {
        emit(_events, ConfirmStakeEvent.NavigateBack)
    }

    fun onCrossClicked() {
        emit(_events, ConfirmStakeEvent.CloseFlow)
    }
}