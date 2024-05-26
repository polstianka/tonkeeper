package com.tonapps.tonkeeper.ui.screen.stake.options

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.ui.screen.stake.model.DetailsArgs
import com.tonapps.tonkeeper.ui.screen.stake.model.ExpandedPoolsArgs
import com.tonapps.tonkeeper.ui.screen.stake.options.StakeOptionsScreensAdapter.Companion.POSITION_DETAILS
import com.tonapps.tonkeeper.ui.screen.stake.options.StakeOptionsScreensAdapter.Companion.POSITION_OPTIONS
import com.tonapps.tonkeeper.ui.screen.stake.options.StakeOptionsScreensAdapter.Companion.POSITION_POOLS
import com.tonapps.wallet.localization.Localization
import core.ResourceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StakeOptionsMainViewModel(
    private val resourceManager: ResourceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(OptionsScreenState())
    val uiState: StateFlow<OptionsScreenState> = _uiState

    private val _singleEvent = MutableSharedFlow<Action>(replay = 0, extraBufferCapacity = 1)
    val singleEvent: Flow<Action> = _singleEvent.asSharedFlow()

    private val _poolsArgs = MutableStateFlow<ExpandedPoolsArgs?>(null)
    val poolsArgs: StateFlow<ExpandedPoolsArgs?> = _poolsArgs

    private val _detailsArgs = MutableStateFlow<DetailsArgs?>(null)
    val detailsArgs: StateFlow<DetailsArgs?> = _detailsArgs

    fun setCurrentPage(index: Int) {
        val header = when (index) {
            POSITION_OPTIONS -> resourceManager.getString(Localization.options)
            POSITION_POOLS -> _poolsArgs.value?.name.orEmpty()
            POSITION_DETAILS -> _detailsArgs.value?.name.orEmpty()
            else -> error("unknown index")
        }
        _uiState.update {
            it.copy(
                currentPage = index,
                headerTitle = header,
            )
        }
    }

    fun onPrevPage() {
        if (_uiState.value.currentPage == 0) {
            emit(Action.Finish)
        } else {
            if (_poolsArgs.value == null) {
                _uiState.update { it.copy(currentPage = POSITION_OPTIONS) }
            } else {
                _uiState.update { it.copy(currentPage = it.currentPage - 1) }
            }
        }
    }

    fun finish() {
        emit(Action.Finish)
    }

    fun setTitle(title: String) {
        _uiState.update { it.copy(headerTitle = title) }
    }

    fun setDetailsArgs(args: DetailsArgs?) {
        _detailsArgs.value = args
    }

    fun setPoolsArgs(args: ExpandedPoolsArgs?) {
        _poolsArgs.value = args
    }

    fun clearPools() {
        _poolsArgs.value = null
    }

    fun destroy() {
        _uiState.value = OptionsScreenState()
        _detailsArgs.value = null
        _poolsArgs.value = null
    }

    private fun emit(action: Action) {
        viewModelScope.launch { _singleEvent.emit(action) }
    }
}

data class OptionsScreenState(
    val headerTitle: CharSequence = "",
    val currentPage: Int = 0,
)

sealed interface Action {
    data object Finish : Action

}