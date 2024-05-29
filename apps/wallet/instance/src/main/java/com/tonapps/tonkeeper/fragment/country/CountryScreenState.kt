package com.tonapps.tonkeeper.fragment.country

import com.tonapps.tonkeeper.fragment.country.list.CountryItem
import uikit.mvi.UiState

data class CountryScreenState(
    val selectedCountry: String = "EE",
    val countries: List<CountryScreenFeature.Country> = emptyList(),
): UiState() {

    val items: List<CountryItem>
        get() {
            val items = mutableListOf<CountryItem>()
            for ((index, country) in countries.withIndex()) {
                val position = com.tonapps.uikit.list.ListCell.getPosition(countries.size, index)
                val selected = country.code == selectedCountry
                items.add(CountryItem(country, selected, position))
            }
            return items
        }
}