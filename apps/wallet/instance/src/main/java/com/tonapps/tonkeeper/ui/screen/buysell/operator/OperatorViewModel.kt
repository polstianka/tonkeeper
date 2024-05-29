package com.tonapps.tonkeeper.ui.screen.buysell.operator

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.network.NetworkMonitor
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.core.fiat.models.FiatItem
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.OperatorRatesRepository
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OperatorViewModel(
    private val networkMonitor: NetworkMonitor,
    private val walletRepository: WalletRepository,
    private val settingsRepository: SettingsRepository,
    private val operatorRatesRepository: OperatorRatesRepository,
    private val api: API,
) : ViewModel() {

    private val _screenStateFlow =
        MutableStateFlow<OperatorScreenState>(OperatorScreenState.initState)
    val screenStateFlow: StateFlow<OperatorScreenState> = _screenStateFlow

    var selectedCurrency = settingsRepository.currency

    init {
        viewModelScope.launch {
            settingsRepository.currencyFlow.collect { walletCurrency ->
                _screenStateFlow.update {
                    it.copy(
                        currency = walletCurrency,
                        currencyNameResId = getNameResIdForCurrency(walletCurrency.code)
                    )
                }

                selectedCurrency = walletCurrency

                // show loading or shimmer
                getOperatorRates(selectedCurrency)
                getPaymentOperators()
            }

        }


    }

    fun getOperatorRates(currency: WalletCurrency) {
        viewModelScope.launch {
            val operatorRates = operatorRatesRepository.getRates(currency)

            _screenStateFlow.update {
                it.copy(
                    operatorRates = operatorRates.associateBy { it.id }
                )
            }
        }
    }

    fun getPaymentOperators() {
        viewModelScope.launch {
            val fiatItemList = getFiatItems()

            _screenStateFlow.update {
                it.copy(
                    fiatItems = fiatItemList
                )
            }
        }
    }

    private suspend fun getFiatItems(): List<FiatItem> {
        val fiatData = App.fiat.getData("EE")
        Log.d("FIAT_METODS", "items: $fiatData")

        val defaultMethods = fiatData?.defaultLayout?.methods
        Log.d("FIAT_METODS", "defaultMethods: $defaultMethods")

        val layoutByCountry = fiatData?.layoutByCountry
        Log.d("FIAT_METODS", "layoutByCountry: $layoutByCountry")

        val methodsBasedOnCurrency = layoutByCountry?.filter {
            it.currency.equals(selectedCurrency.code, ignoreCase = true)
        }?.firstOrNull()?.methods
        Log.d("FIAT_METODS", "selectedCurrency.code: ${selectedCurrency.code}")
        Log.d("FIAT_METODS", "methodsBasedOnCurrency: $methodsBasedOnCurrency")

        return if (methodsBasedOnCurrency != null) {
            fiatData.getBuyItemsByMethods(methodsBasedOnCurrency)
        } else if (defaultMethods != null) {
            fiatData.getBuyItemsByMethods(defaultMethods)
        } else emptyList()

    }


    @StringRes
    private fun getNameResIdForCurrency(currency: String): Int {
        return when (currency.lowercase()) {
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