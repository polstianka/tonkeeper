package com.tonapps.tonkeeper.ui.screen.buysell.country

import androidx.lifecycle.viewModelScope
import com.tonapps.network.NetworkMonitor
import com.tonapps.tonkeeper.event.ChangeCountryEvent
import com.tonapps.tonkeeper.extensions.flagEmoji
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.TokenRepository
import core.EventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uikit.mvi.UiFeature
import java.util.Locale

class BuySellCountryScreenFeature(
    private val networkMonitor: NetworkMonitor,
    private val walletRepository: WalletRepository,
    private val settingsRepository: SettingsRepository,
    private val tokenRepository: TokenRepository,
    private val api: API,
) : UiFeature<BuySellCountryScreenState, BuySellCountryScreenEffect>(BuySellCountryScreenState()) {


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