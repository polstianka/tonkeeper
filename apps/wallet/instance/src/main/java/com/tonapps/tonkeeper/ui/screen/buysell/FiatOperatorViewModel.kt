package com.tonapps.tonkeeper.ui.screen.buysell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.core.fiat.Fiat
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.settings.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FiatOperatorViewModel(
    private val settingsRepository: SettingsRepository,
    private val fiat: Fiat
) : ViewModel() {

    private val _uiState = MutableStateFlow(FiatOperatorUiState())
    val uiState: StateFlow<FiatOperatorUiState> = _uiState

    private var opType: FiatOperation = FiatOperation.Buy

    fun init(operationType: FiatOperation) {
        opType = operationType
        viewModelScope.launch(Dispatchers.IO) {
            val country = settingsRepository.country
            val methods = if (operationType == FiatOperation.Buy) fiat.getBuyMethods(country)
            else fiat.getSellMethods(country)
            val mappedMethods = methods.mapIndexed { index, fiatItem ->
                val pattern = if (fiatItem.successUrlPattern != null) {
                    fiatItem.successUrlPattern.toJSON().toString()
                } else null
                Method(
                    id = fiatItem.id,
                    name = fiatItem.title,
                    subtitle = fiatItem.subtitle,
                    selected = index == 0,
                    position = ListCell.getPosition(methods.size, index),
                    iconUrl = fiatItem.iconUrl,
                    url = fiatItem.actionButton.url,
                    pattern = pattern
                )
            }
            _uiState.update {
                it.copy(methods = mappedMethods, currency = settingsRepository.currency.code)
            }
        }
    }

    fun reloadMethods() {
        init(opType)
    }

    fun selectMethod(id: String) {
        _uiState.update { state ->
            val newMethodList = state.methods.map { it.copy(selected = it.id == id) }
            state.copy(methods = newMethodList)
        }
    }

    fun getSelected(): Method {
        return uiState.value.methods.first { it.selected }
    }
}

data class FiatOperatorUiState(
    val methods: List<Method> = emptyList(),
    val currency: String = "usd"
)

data class Method(
    val id: String,
    val name: String,
    val subtitle: String,
    val selected: Boolean,
    val position: ListCell.Position,
    val iconUrl: String,
    val url: String,
    val pattern: String?
) : BaseListItem()
