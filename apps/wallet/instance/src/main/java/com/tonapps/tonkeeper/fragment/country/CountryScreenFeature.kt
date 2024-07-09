package com.tonapps.tonkeeper.fragment.country

import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.extensions.flagEmoji
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uikit.mvi.UiFeature
import java.util.Locale

class CountryScreenFeature: UiFeature<CountryScreenState, CountryScreenEffect>(CountryScreenState()) {

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
        com.tonapps.tonkeeper.App.settings.country = code

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