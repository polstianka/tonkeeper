package com.tonapps.tonkeeper.ui.screen.buysell.currency

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.ui.screen.buysell.currency.list.BuySellCurrencyItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class BuySellCurrencyViewModel(
    private val settings: SettingsRepository
): ViewModel() {

    private val _uiItemsFlow = MutableStateFlow<List<BuySellCurrencyItem>>(emptyList())
    val uiItemsFlow = _uiItemsFlow.asStateFlow().filter { it.isNotEmpty() }

    init {
        settings.currencyFlow.map { buildUiItems(it) }.onEach {
            _uiItemsFlow.value = it
        }.launchIn(viewModelScope)
    }

    fun selectCurrency(currency: String) {
        settings.currency = WalletCurrency(currency)
    }

    private fun buildUiItems(selectedCurrency: WalletCurrency): List<BuySellCurrencyItem> {
        val currencies = WalletCurrency.FIAT
        val items = mutableListOf<BuySellCurrencyItem>()
        for ((index, currency) in currencies.withIndex()) {
            val item = BuySellCurrencyItem(
                currency = currency,
                nameResId = getNameResIdForCurrency(currency),
                selected = currency == selectedCurrency.code,
                position = ListCell.getPosition(currencies.size, index)
            )
            items.add(item)
        }
        return items
    }

    @StringRes
    private fun getNameResIdForCurrency(currency: String): Int {
        return when(currency.lowercase()) {
            "usd" -> Localization.currency_usd_name
            "eur" -> Localization.currency_eur_name
            "rub" -> Localization.currency_rub_name
            "aed" -> Localization.currency_aed_name
            "uah" -> Localization.currency_uah_name
            "kzt" -> Localization.currency_kzt_name
            "uzs" -> Localization.currency_uzs_name
            "gbp" -> Localization.currency_gbp_name
            "chf" -> Localization.currency_chf_name
            "cny" -> Localization.currency_cny_name
            "krw" -> Localization.currency_krw_name
            "idr" -> Localization.currency_idr_name
            "inr" -> Localization.currency_inr_name
            "jpy" -> Localization.currency_jpy_name
            "cad" -> Localization.currency_cad_name
            "ars" -> Localization.currency_ars_name
            "byn" -> Localization.currency_byn_name
            "cop" -> Localization.currency_cop_name
            "etb" -> Localization.currency_etb_name
            "ils" -> Localization.currency_ils_name
            "kes" -> Localization.currency_kes_name
            "ngn" -> Localization.currency_ngn_name
            "ugx" -> Localization.currency_ugx_name
            "ves" -> Localization.currency_ves_name
            "zar" -> Localization.currency_zar_name
            "try" -> Localization.currency_try_name
            "thb" -> Localization.currency_thb_name
            "vnd" -> Localization.currency_vnd_name
            "brl" -> Localization.currency_brl_name
            "gel" -> Localization.currency_gel_name
            "bdt" -> Localization.currency_bdt_name

            "ton" -> Localization.toncoin
            "btc" -> Localization.bitcoin
            else -> throw IllegalArgumentException("Unsupported currency: $currency")
        }

    }

}