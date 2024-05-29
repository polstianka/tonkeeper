package com.tonapps.tonkeeper.ui.screen.buysell.country

import com.tonapps.tonkeeper.ui.screen.buysell.country.list.BuySellCountryItem
import uikit.mvi.UiState

data class BuySellCountryScreenState(
    val selectedCountry: String = "EE",
    val countries: List<BuySellCountryScreenFeature.Country> = emptyList(),
): UiState() {

    val items: List<BuySellCountryItem>
        get() {
            val items = mutableListOf<BuySellCountryItem>()
            for ((index, country) in countries.withIndex()) {
                val position = com.tonapps.uikit.list.ListCell.getPosition(countries.size, index)
                val selected = country.code == selectedCountry
                items.add(BuySellCountryItem(country, selected, position))
            }
            return items
        }
}