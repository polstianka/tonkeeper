package com.tonapps.tonkeeper.fragment.country

import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.event.ChangeCountryEvent
import com.tonapps.tonkeeper.extensions.flagEmoji
import com.tonapps.tonkeeper.koin.settingsRepository
import com.tonapps.wallet.data.settings.SettingsRepository
import core.EventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uikit.mvi.UiFeature
import java.util.Locale

@Deprecated("Need refactoring")
class CountryScreenFeature(
    private val settingsRepository: SettingsRepository
): UiFeature<CountryScreenState, CountryScreenEffect>(CountryScreenState()) {

    private companion object {
        private var countries = listOf<Country>()
    }

    data class Country(
        val code: String,
        val title: String,
        val emoji: String,
    )

    fun load() {
        viewModelScope.launch {
            if (countries.isEmpty()) {
                countries = loadCountries()
            }

            updateUiState { currentState ->
                currentState.copy(
                    countries = countries.toList()
                )
            }
        }
    }

    fun setSelection(code: String) {
        settingsRepository.country = code
        EventBus.post(ChangeCountryEvent(code))

        updateUiState { currentState ->
            currentState.copy(
                selectedCountry = code
            )
        }
    }

    fun search(q: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val foundCountries = countries.filter { it.title.contains(q, true) }
            updateUiState { currentState ->
                currentState.copy(
                    countries = foundCountries
                )
            }
        }
    }

    private suspend fun loadCountries(): List<Country> = withContext(Dispatchers.IO) {
        val locales = Locale.getISOCountries()
        val countries = mutableListOf<Country>()
        for (countryCode in locales) {
            val locale = Locale("", countryCode)
            val countryName = locale.displayName
            val countryEmoji = locale.flagEmoji
            countries.add(Country(countryCode, countryName, countryEmoji))
        }
        return@withContext countries
    }

}