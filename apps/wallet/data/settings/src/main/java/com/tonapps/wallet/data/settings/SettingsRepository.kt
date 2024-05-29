package com.tonapps.wallet.data.settings

import android.content.Context
import com.tonapps.extensions.MutableEffectFlow
import com.tonapps.extensions.locale
import com.tonapps.wallet.data.core.SearchEngine
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.localization.Language
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsRepository(
    private val context: Context
) {

    companion object {
        const val DEFAULT_SLIPPAGE = 0.005f
        const val DEFAULT_PRICE_IMPACT = 0.05f

        private const val NAME = "settings"
        private const val CURRENCY_CODE_KEY = "currency_code"
        private const val LOCK_SCREEN_KEY = "lock_screen"
        private const val BIOMETRIC_KEY = "biometric"
        private const val COUNTRY_KEY = "country"
        private const val LANGUAGE_CODE_KEY = "language_code"
        private const val THEME_KEY = "theme"
        private const val HIDDEN_BALANCES_KEY = "hidden_balances"
        private const val FIREBASE_TOKEN_KEY = "firebase_token"
        private const val INSTALL_ID_KEY = "install_id"
        private const val SEARCH_ENGINE_KEY = "search_engine"

        private const val SWAP_SLIPPAGE = "swap_slippage"
        private const val SWAP_SLIPPAGE_USE_CUSTOM = "swap_slippage_use_custom"
        private const val SWAP_PRICE_IMPACT = "swap_price_impact"
        private const val SWAP_PRICE_IMPACT_USE_CUSTOM = "swap_price_impact_use_custom"
    }

    data class SwapSettings(
        val slippage: Float,
        val slippageUseCustom: Boolean,
        val priceImpact: Float,
        val priceImpactUseCustom: Boolean,
    )

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _currencyFlow = MutableEffectFlow<WalletCurrency>()
    val currencyFlow = _currencyFlow.stateIn(scope, SharingStarted.Eagerly, null).filterNotNull()

    private val _languageFlow = MutableEffectFlow<Language>()
    val languageFlow = _languageFlow.stateIn(scope, SharingStarted.Eagerly, null).filterNotNull()

    private val _themeFlow = MutableEffectFlow<String>()
    val themeFlow = _themeFlow.stateIn(scope, SharingStarted.Eagerly, null).filterNotNull()

    private val _hiddenBalancesFlow = MutableEffectFlow<Boolean>()
    val hiddenBalancesFlow = _hiddenBalancesFlow.stateIn(scope, SharingStarted.Eagerly, null).filterNotNull()

    private val _firebaseTokenFlow = MutableEffectFlow<String?>()
    val firebaseTokenFlow = _firebaseTokenFlow.stateIn(scope, SharingStarted.Eagerly, null).filterNotNull()

    private val _countryFlow = MutableEffectFlow<String>()
    val countryFlow = _countryFlow.stateIn(scope, SharingStarted.Eagerly, null).filterNotNull()

    private val _searchEngineFlow = MutableEffectFlow<SearchEngine>()
    val searchEngineFlow = _searchEngineFlow.stateIn(scope, SharingStarted.Eagerly, null).filterNotNull()

    private val _swapSettingsFlow = MutableEffectFlow<SwapSettings?>()
    val swapSettingsFlow = _swapSettingsFlow.stateIn(scope, SharingStarted.Eagerly, null).filterNotNull()

    private val prefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    val installId: String
        get() = prefs.getString(INSTALL_ID_KEY, null) ?: run {
            val id = java.util.UUID.randomUUID().toString()
            prefs.edit().putString(INSTALL_ID_KEY, id).apply()
            id
        }

    var searchEngine: SearchEngine = SearchEngine(prefs.getString(SEARCH_ENGINE_KEY, "Google")!!)
        set(value) {
            if (value != field) {
                prefs.edit().putString(SEARCH_ENGINE_KEY, value.title).apply()
                field = value
                _searchEngineFlow.tryEmit(value)
            }
        }

    var theme: String = prefs.getString(THEME_KEY, "blue")!!
        set(value) {
            if (value != field) {
                prefs.edit().putString(THEME_KEY, value).apply()
                field = value
                _themeFlow.tryEmit(value)
            }
        }

    var firebaseToken: String? = prefs.getString(FIREBASE_TOKEN_KEY, null)
        set(value) {
            if (value != field) {
                prefs.edit().putString(FIREBASE_TOKEN_KEY, value).apply()
                field = value
                _firebaseTokenFlow.tryEmit(value)
            }
        }

    var currency: WalletCurrency = WalletCurrency(prefs.getString(CURRENCY_CODE_KEY, WalletCurrency.FIAT.first())!!)
        set(value) {
            if (field != value) {
                prefs.edit().putString(CURRENCY_CODE_KEY, value.code).apply()
                field = value
                _currencyFlow.tryEmit(value)
            }
        }

    var language: Language = Language(prefs.getString(LANGUAGE_CODE_KEY, "en") ?: Language.DEFAULT)
        set(value) {
            if (value != field) {
                field = value
                prefs.edit().putString(LANGUAGE_CODE_KEY, field.code).apply()
                _languageFlow.tryEmit(field)
            }
        }

    var lockScreen: Boolean = prefs.getBoolean(LOCK_SCREEN_KEY, false)
        set(value) {
            if (value != field) {
                prefs.edit().putBoolean(LOCK_SCREEN_KEY, value).apply()
                field = value
            }
        }

    var biometric: Boolean = prefs.getBoolean(BIOMETRIC_KEY, false)
        set(value) {
            if (value != field) {
                prefs.edit().putBoolean(BIOMETRIC_KEY, value).apply()
                field = value
            }
        }

    var country: String = prefs.getString(COUNTRY_KEY, null) ?: context.locale.country
        set(value) {
            if (value != field) {
                prefs.edit().putString(COUNTRY_KEY, value).apply()
                field = value
                _countryFlow.tryEmit(value)
            }
        }

    var hiddenBalances: Boolean = prefs.getBoolean(HIDDEN_BALANCES_KEY, false)
        set(value) {
            if (value != field) {
                prefs.edit().putBoolean(HIDDEN_BALANCES_KEY, value).apply()
                field = value
                _hiddenBalancesFlow.tryEmit(value)
            }
        }

    var swapSettings: SwapSettings = loadSwapSettings()
        set(value) {
            saveSwapSettings(value)
            field = value
            _swapSettingsFlow.tryEmit(value)
        }

    private fun loadSwapSettings(): SwapSettings {
        return SwapSettings(
            slippage = prefs.getFloat(SWAP_SLIPPAGE, DEFAULT_SLIPPAGE),
            slippageUseCustom = prefs.getBoolean(SWAP_SLIPPAGE_USE_CUSTOM, false),
            priceImpact = prefs.getFloat(SWAP_PRICE_IMPACT, DEFAULT_PRICE_IMPACT),
            priceImpactUseCustom = prefs.getBoolean(SWAP_PRICE_IMPACT_USE_CUSTOM, false)
        )
    }

    private fun saveSwapSettings(value: SwapSettings) {
        val editor = prefs.edit()
        editor.putFloat(SWAP_SLIPPAGE, value.slippage)
        editor.putBoolean(SWAP_SLIPPAGE_USE_CUSTOM, value.slippageUseCustom)
        editor.putFloat(SWAP_PRICE_IMPACT, value.priceImpact)
        editor.putBoolean(SWAP_PRICE_IMPACT_USE_CUSTOM, value.priceImpactUseCustom)
        editor.apply()
    }

    init {
        scope.launch(Dispatchers.IO) {
            _currencyFlow.tryEmit(currency)
            _themeFlow.tryEmit(theme)
            _languageFlow.tryEmit(language)
            _hiddenBalancesFlow.tryEmit(hiddenBalances)
            _firebaseTokenFlow.tryEmit(firebaseToken)
            _countryFlow.tryEmit(country)
            _searchEngineFlow.tryEmit(searchEngine)
            _swapSettingsFlow.tryEmit(swapSettings)
        }
    }
}