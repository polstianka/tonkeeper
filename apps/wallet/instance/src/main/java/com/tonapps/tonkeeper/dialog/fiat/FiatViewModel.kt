package com.tonapps.tonkeeper.dialog.fiat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.core.fiat.Fiat
import com.tonapps.tonkeeper.core.fiat.models.FiatData
import com.tonapps.tonkeeper.core.fiat.models.FiatItem
import com.tonapps.tonkeeper.core.fiat.models.FiatSuccessUrlPattern
import com.tonapps.wallet.data.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class FiatViewModel(
    private val fiat: Fiat,
    private val settings: SettingsRepository
) : ViewModel() {

    private val _events = MutableSharedFlow<Action>(replay = 0, extraBufferCapacity = 1)
    val events: Flow<Action> = _events

    private val _items = MutableStateFlow(FiatUiState())
    val items: Flow<FiatUiState> = _items.filterNotNull()

    init {
        viewModelScope.launch {
            val data = fiat.getData(settings.country)
            val methods = fiat.getBuyMethods(settings.country)
            _items.value = FiatUiState(data, methods)
        }
    }

    fun openItem(item: FiatItem) {
        viewModelScope.launch {
            if (fiat.isShowConfirmation(item.id)) {
                _events.emit(Action.ConfirmationDialog(item))
            } else {
                _events.emit(Action.OpenUrl(item.actionButton.url, item.successUrlPattern))
            }
        }
    }

    fun disableShowConfirmation(item: FiatItem) {
        viewModelScope.launch {
            fiat.disableShowConfirmation(item.id)
        }
    }
}

data class FiatUiState(
    val data: FiatData? = null,
    val methods: List<FiatItem> = emptyList(),
)

sealed interface Action {
    data class ConfirmationDialog(val item: FiatItem) : Action
    data class OpenUrl(
        val url: String,
        val pattern: FiatSuccessUrlPattern?
    ) : Action

}