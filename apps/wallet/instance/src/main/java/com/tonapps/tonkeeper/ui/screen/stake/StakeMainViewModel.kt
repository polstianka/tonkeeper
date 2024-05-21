package com.tonapps.tonkeeper.ui.screen.stake

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.ui.screen.stake.confirm.ConfirmationArgs
import com.tonapps.wallet.localization.Localization
import core.ResourceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StakeMainViewModel(
    private val resourceManager: ResourceManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(StakeScreenState())
    val uiState: StateFlow<StakeScreenState> = _uiState

    private val _singleEvent = MutableSharedFlow<Action>(replay = 0, extraBufferCapacity = 1)
    val singleEvent: Flow<Action> = _singleEvent.asSharedFlow()

    private val _confirmationArgs = MutableStateFlow<ConfirmationArgs?>(null)
    val confirmationArgs: StateFlow<ConfirmationArgs?> = _confirmationArgs

    var preselectedAddress: String? = null

    fun setCurrentPage(index: Int, unstake: Boolean) {
        val header = when (index) {
            StakeScreensAdapter.POSITION_AMOUNT -> resourceManager.getString(if (unstake) Localization.unstake else Localization.stake)
            else -> ""
        }
        _uiState.update { it.copy(currentPage = index, headerTitle = header) }
    }

    fun onCloseClick() {
        when (_uiState.value.currentPage) {
            0 -> emit(Action.Info)
            else -> _uiState.update { it.copy(currentPage = it.currentPage - 1) }
        }
    }

    fun onConfirmationArgsReceived(confirmScreenArgs: ConfirmationArgs) {
        _confirmationArgs.value = confirmScreenArgs
        onNextPage()
    }

    fun destroy() {
        _uiState.value = StakeScreenState()
        _confirmationArgs.value = null
    }

    fun openOptions() {
        emit(Action.Options)
    }

    private fun onNextPage() {
        _uiState.update { it.copy(currentPage = it.currentPage + 1) }
    }

    private fun emit(action: Action) {
        viewModelScope.launch { _singleEvent.emit(action) }
    }
}

data class StakeScreenState(
    val headerTitle: CharSequence = "",
    val currentPage: Int = 0
)

sealed interface Action {
    data object Info : Action
    data object Options : Action
}