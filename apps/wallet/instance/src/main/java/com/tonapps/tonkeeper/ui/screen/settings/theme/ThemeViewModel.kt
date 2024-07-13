package com.tonapps.tonkeeper.ui.screen.settings.theme

import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.ui.screen.settings.theme.list.Item
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.core.Theme
import com.tonapps.wallet.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import uikit.extensions.collectFlow

class ThemeViewModel(
    private val settingsRepository: SettingsRepository
): ViewModel() {

    private val _uiItemsFlow = MutableStateFlow<List<Item>>(emptyList())
    val uiItemsFlow = _uiItemsFlow.asStateFlow().filter { it.isNotEmpty() }

    init {
        collectFlow(settingsRepository.themeFlow) { theme ->
            updateValues(theme.resId)
        }
    }

    fun setTheme(theme: Int) {
        settingsRepository.theme = Theme.getByResId(theme)
    }

    private fun updateValues(themeId: Int) {
        val items = mutableListOf<Item>()
        for ((index, theme) in Theme.getSupported().withIndex()) {
            val position = ListCell.getPosition(Theme.getSupported().size, index)
            items.add(Item(
                position = position,
                theme = theme,
                selected = themeId == theme.resId
            ))
        }
        _uiItemsFlow.value = items
    }
}