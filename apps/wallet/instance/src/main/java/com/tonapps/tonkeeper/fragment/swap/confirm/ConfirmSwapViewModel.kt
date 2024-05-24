package com.tonapps.tonkeeper.fragment.swap.confirm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.core.emit
import com.tonapps.tonkeeper.fragment.swap.domain.CreateStonfiSwapMessageCase
import com.tonapps.wallet.data.account.legacy.WalletManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ConfirmSwapViewModel(
    private val walletManager: WalletManager,
    private val createStonfiSwapMessageCase: CreateStonfiSwapMessageCase
) : ViewModel() {
    private val _args = MutableSharedFlow<ConfirmSwapArgs>(replay = 1)
    private val _events = MutableSharedFlow<ConfirmSwapEvent>()

    val events: Flow<ConfirmSwapEvent>
        get() = _events
    val args: Flow<ConfirmSwapArgs>
        get() = _args

    fun provideArgs(args: ConfirmSwapArgs) {
        emit(this._args, args)
    }

    fun onCloseClicked() {
        emit(_events, ConfirmSwapEvent.CloseFlow)
    }

    fun onConfirmClicked() = viewModelScope.launch {
        val args = _args.first()
        val wallet = walletManager.getWalletInfo()!!
        val cell = createStonfiSwapMessageCase.execute(
            args.sendAsset,
            args.receiveAsset,
            args.amount,
            wallet,
            args.simulation
        )
        val isSuccess = cell != null
        if (isSuccess) {
            _events.emit(ConfirmSwapEvent.CloseFlow)
        }
    }

    fun onCancelClicked() {
        emit(_events, ConfirmSwapEvent.NavigateBack)
    }
}