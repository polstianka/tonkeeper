package com.tonapps.tonkeeper.fragment.swap.confirm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.core.emit
import com.tonapps.tonkeeper.fragment.swap.domain.CreateStonfiSwapMessageCase
import com.tonapps.wallet.data.account.legacy.WalletManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import uikit.widget.ProcessTaskView

class ConfirmSwapViewModel(
    private val walletManager: WalletManager,
    private val createStonfiSwapMessageCase: CreateStonfiSwapMessageCase
) : ViewModel() {
    private val _args = MutableSharedFlow<ConfirmSwapArgs>(replay = 1)
    private val _events = MutableSharedFlow<ConfirmSwapEvent>()
    private val _isLoading = MutableStateFlow(false)
    private val _loaderState = MutableStateFlow(ProcessTaskView.State.LOADING)

    val events: Flow<ConfirmSwapEvent>
        get() = _events
    val args: Flow<ConfirmSwapArgs>
        get() = _args
    val isLoading: Flow<Boolean>
        get() = _isLoading
    val loaderState: Flow<ProcessTaskView.State>
        get() = _loaderState

    fun provideArgs(args: ConfirmSwapArgs) {
        emit(this._args, args)
    }

    fun onCloseClicked() {
        emit(_events, ConfirmSwapEvent.FinishFlow(false))
    }

    fun onConfirmClicked() = viewModelScope.launch {
        _isLoading.value = true
        _loaderState.value = ProcessTaskView.State.LOADING
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
            _loaderState.value = ProcessTaskView.State.SUCCESS
            delay(1000L)
            _events.emit(ConfirmSwapEvent.FinishFlow(true))
        } else {
            _loaderState.value = ProcessTaskView.State.FAILED
            delay(1000L)
        }
        _isLoading.value = false
    }

    fun onCancelClicked() {
        emit(_events, ConfirmSwapEvent.NavigateBack)
    }
}