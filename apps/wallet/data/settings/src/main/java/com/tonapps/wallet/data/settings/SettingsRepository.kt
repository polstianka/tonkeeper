package com.tonapps.wallet.data.settings

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.tonapps.extensions.MutableEffectFlow
import com.tonapps.extensions.clear
import com.tonapps.extensions.locale
import com.tonapps.wallet.data.core.SearchEngine
import com.tonapps.wallet.data.core.Theme
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.core.isAvailableBiometric
import com.tonapps.wallet.data.rn.RNLegacy
import com.tonapps.wallet.data.settings.entities.TokenPrefsEntity
import com.tonapps.wallet.data.settings.folder.TokenPrefsFolder
import com.tonapps.wallet.data.settings.folder.WalletPrefsFolder
import com.tonapps.wallet.localization.Language
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

// TODO need to be refactored
class SettingsRepository(
    private val scope: CoroutineScope,
    private val context: Context,
    private val rnLegacy: RNLegacy,
) {

    private companion object {
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
        private const val ENCRYPTED_COMMENT_MODAL_KEY = "encrypted_comment_modal"
    }

    private val _currencyFlow = MutableEffectFlow<WalletCurrency>()
    val currencyFlow = _currencyFlow.stateIn(scope, SharingStarted.Eagerly, null).filterNotNull().distinctUntilChanged()

    private val _languageFlow = MutableEffectFlow<Language>()
    val languageFlow = _languageFlow.stateIn(scope, SharingStarted.Eagerly, null).filterNotNull()

    private val _themeFlow = MutableEffectFlow<Theme>()
    val themeFlow = _themeFlow.stateIn(scope, SharingStarted.Eagerly, null).filterNotNull()

    private val _hiddenBalancesFlow = MutableEffectFlow<Boolean>()
    val hiddenBalancesFlow = _hiddenBalancesFlow.stateIn(scope, SharingStarted.Eagerly, null).filterNotNull()

    private val _firebaseTokenFlow = MutableEffectFlow<String?>()
    val firebaseTokenFlow = _firebaseTokenFlow.stateIn(scope, SharingStarted.Eagerly, null).filterNotNull()

    private val _countryFlow = MutableEffectFlow<String>()
    val countryFlow = _countryFlow.stateIn(scope, SharingStarted.Eagerly, null).filterNotNull().map { fixCountryCode(it) }

    private val _biometricFlow = MutableStateFlow<Boolean?>(null)
    val biometricFlow = _biometricFlow.stateIn(scope, SharingStarted.Eagerly, null).filterNotNull()

    private val _lockscreenFlow = MutableStateFlow<Boolean?>(null)
    val lockscreenFlow = _lockscreenFlow.stateIn(scope, SharingStarted.Eagerly, null).filterNotNull()

    private val _searchEngineFlow = MutableEffectFlow<SearchEngine>()
    val searchEngineFlow = _searchEngineFlow.stateIn(scope, SharingStarted.Eagerly, null).filterNotNull()

    private val _walletPush = MutableEffectFlow<Unit>()
    val walletPush = _walletPush.shareIn(scope, SharingStarted.Eagerly)

    private val prefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    private val tokenPrefsFolder = TokenPrefsFolder(context, scope)
    private val walletPrefsFolder = WalletPrefsFolder(context, scope)
    private val migrationHelper = RNMigrationHelper(scope, context, rnLegacy)

    val walletPrefsChangedFlow: Flow<Unit>
        get() = walletPrefsFolder.changedFlow

    val tokenPrefsChangedFlow: Flow<Unit>
        get() = tokenPrefsFolder.changedFlow

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
                migrationHelper.setLegacySearchEngine(value)
            }
        }

    var theme: Theme = Theme.getByKey(prefs.getString(THEME_KEY, "blue")!!)
        set(value) {
            if (value != field) {
                prefs.edit().putString(THEME_KEY, value.key).apply()
                field = value
                _themeFlow.tryEmit(value)
                migrationHelper.setLegacyTheme(value)
            }
        }

    var showEncryptedCommentModal: Boolean = prefs.getBoolean(ENCRYPTED_COMMENT_MODAL_KEY, true)
        set(value) {
            if (value != field) {
                prefs.edit().putBoolean(ENCRYPTED_COMMENT_MODAL_KEY, value).apply()
                field = value
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

    var currency: WalletCurrency = WalletCurrency.of(prefs.getString(CURRENCY_CODE_KEY, null))
        set(value) {
            if (field != value && value.code.isNotEmpty()) {
                prefs.edit().putString(CURRENCY_CODE_KEY, value.code).apply()
                field = value
                _currencyFlow.tryEmit(value)
                migrationHelper.setLegacyCurrency(value)
            }
        }

    var language: Language = Language(prefs.getString(LANGUAGE_CODE_KEY, "en") ?: Language.DEFAULT)
        set(value) {
            if (value != field) {
                field = value
                prefs.edit().putString(LANGUAGE_CODE_KEY, field.code).apply()
                _languageFlow.tryEmit(field)
                migrationHelper.setLegacyLanguage(value)
            }
        }

    var lockScreen: Boolean = prefs.getBoolean(LOCK_SCREEN_KEY, false)
        set(value) {
            if (value != field) {
                prefs.edit().putBoolean(LOCK_SCREEN_KEY, value).apply()
                field = value
                _lockscreenFlow.tryEmit(value)
                migrationHelper.setLockScreenEnabled(value)
            }
        }

    var biometric: Boolean = if (isAvailableBiometric(context)) prefs.getBoolean(BIOMETRIC_KEY, false) else false
        set(value) {
            if (value != field) {
                prefs.edit().putBoolean(BIOMETRIC_KEY, value).apply()
                field = value
                _biometricFlow.tryEmit(value)
                migrationHelper.setBiometryEnabled(value)
            }
        }

    var country: String = fixCountryCode(prefs.getString(COUNTRY_KEY, null))
        set(value) {
            if (value != field) {
                prefs.edit().putString(COUNTRY_KEY, value).apply()
                field = value
                _countryFlow.tryEmit(value)
                migrationHelper.setLegacySelectedCountry(value)
            }
        }

    var hiddenBalances: Boolean = prefs.getBoolean(HIDDEN_BALANCES_KEY, false)
        set(value) {
            if (value != field) {
                prefs.edit().putBoolean(HIDDEN_BALANCES_KEY, value).apply()
                field = value
                _hiddenBalancesFlow.tryEmit(value)
                migrationHelper.setHiddenBalance(value)
            }
        }

    fun getSpamStateTransaction(walletId: String, id: String) = walletPrefsFolder.getSpamStateTransaction(walletId, id)

    fun isSpamTransaction(walletId: String, id: String) = getSpamStateTransaction(walletId, id) == SpamTransactionState.SPAM

    fun isPurchaseOpenConfirm(walletId: String, id: String) = walletPrefsFolder.isPurchaseOpenConfirm(walletId, id)

    fun disablePurchaseOpenConfirm(walletId: String, id: String) = walletPrefsFolder.disablePurchaseOpenConfirm(walletId, id)

    fun getPushWallet(walletId: String): Boolean = walletPrefsFolder.isPushEnabled(walletId)

    fun setPushWallet(walletId: String, value: Boolean) {
        walletPrefsFolder.setPushEnabled(walletId, value)
        rnLegacy.setNotificationsEnabled(walletId, value)
        _walletPush.tryEmit(Unit)
    }

    fun isBatteryViewed(): Boolean = walletPrefsFolder.isBatteryViewed()

    fun setBatteryViewed() = walletPrefsFolder.setBatteryViewed()

    fun isSetupHidden(walletId: String): Boolean = walletPrefsFolder.isSetupHidden(walletId)

    fun setupHide(walletId: String) {
        walletPrefsFolder.setupHide(walletId)
        rnLegacy.setSetupDismissed(walletId)
    }

    fun setTelegramChannel(walletId: String) {
        walletPrefsFolder.setTelegramChannel(walletId)
        rnLegacy.setHasOpenedTelegramChannel(walletId)
    }

    fun isTelegramChannel(walletId: String) = walletPrefsFolder.isTelegramChannel(walletId)

    fun setLastBackupAt(walletId: String, date: Long) {
        rnLegacy.setSetupLastBackupAt(walletId, date)
    }

    fun getLocale(): Locale {
        if (language.code == Language.DEFAULT) {
            return context.locale
        }
        return language.locale
    }

    suspend fun setTokenHidden(
        walletId: String,
        tokenAddress: String,
        hidden: Boolean
    ) = withContext(Dispatchers.IO) {
        tokenPrefsFolder.setHidden(walletId, tokenAddress, hidden)
        rnLegacy.setTokenHidden(walletId, tokenAddress, hidden)
    }

    suspend fun setTokenState(
        walletId: String,
        tokenAddress: String,
        state: TokenPrefsEntity.State
    ) = withContext(Dispatchers.IO) {
        tokenPrefsFolder.setState(walletId, tokenAddress, state)
    }

    fun setTokenPinned(walletId: String, tokenAddress: String, pinned: Boolean) {
        tokenPrefsFolder.setPinned(walletId, tokenAddress, pinned)
    }

    fun setTokensSort(walletId: String, tokensAddress: List<String>) {
        tokenPrefsFolder.setSort(walletId, tokensAddress)
    }

    fun setWalletsSort(walletIds: List<String>) {
        walletPrefsFolder.setSort(walletIds)
    }

    fun getWalletPrefs(walletId: String) = walletPrefsFolder.get(walletId)

    fun getWalletLastUpdated(walletId: String) = walletPrefsFolder.getLastUpdated(walletId)

    fun setWalletLastUpdated(walletId: String) {
        walletPrefsFolder.setLastUpdated(walletId)
    }

    suspend fun getTokenPrefs(
        walletId: String,
        tokenAddress: String,
        blacklist: Boolean = false,
    ): TokenPrefsEntity = withContext(Dispatchers.IO) {
        tokenPrefsFolder.get(walletId, tokenAddress, blacklist)
    }

    init {
        languageFlow.onEach {
            AppCompatDelegate.setApplicationLocales(getLocales())
        }.launchIn(scope)

        scope.launch(Dispatchers.IO) {
            if (rnLegacy.isRequestMigration()) {
                prefs.clear()
                tokenPrefsFolder.clear()
                walletPrefsFolder.clear()

                val legacyValues = importFromLegacy()
                biometric = legacyValues.biometric
                lockScreen = legacyValues.lockScreen
                currency = if (legacyValues.currency.code.isBlank()) {
                    WalletCurrency.DEFAULT
                } else {
                    legacyValues.currency
                }
                theme = legacyValues.theme
                language = legacyValues.language
                hiddenBalances = legacyValues.hiddenBalances
                country = legacyValues.country
                searchEngine = legacyValues.searchEngine
            }

            _currencyFlow.tryEmit(currency)
            _themeFlow.tryEmit(theme)
            _languageFlow.tryEmit(language)
            _hiddenBalancesFlow.tryEmit(hiddenBalances)
            _firebaseTokenFlow.tryEmit(firebaseToken)
            _countryFlow.tryEmit(country)
            _searchEngineFlow.tryEmit(searchEngine)
            _biometricFlow.tryEmit(biometric)
            _lockscreenFlow.tryEmit(lockScreen)
            _walletPush.tryEmit(Unit)
        }
    }

    private data class LegacyValues(
        val currency: WalletCurrency,
        val language: Language,
        val searchEngine: SearchEngine,
        val theme: Theme,
        val hiddenBalances: Boolean,
        val country: String,
        val lockScreen: Boolean,
        val biometric: Boolean,
    )

    private suspend fun importFromLegacy(): LegacyValues {
        importLegacyWallets()
        return LegacyValues(
            currency = migrationHelper.getLegacyCurrency(),
            language = migrationHelper.getLegacyLanguage(),
            searchEngine = migrationHelper.getLegacySearchEngine(),
            theme = migrationHelper.getLegacyTheme(),
            hiddenBalances = migrationHelper.getHiddenBalances(),
            country = migrationHelper.getLegacySelectedCountry(),
            lockScreen = migrationHelper.getLockScreenEnabled(),
            biometric = migrationHelper.getBiometryEnabled(),
        )
    }

    private suspend fun importLegacyWallets() {
        val data = rnLegacy.getWallets()

        val wallets = data.wallets
        for (wallet in wallets) {
            val walletId = wallet.identifier

            walletPrefsFolder.setPushEnabled(walletId, rnLegacy.getNotificationsEnabled(walletId))

            val hiddenTokens = rnLegacy.getHiddenTokens(walletId)
            for (tokenAddress in hiddenTokens) {
                tokenPrefsFolder.setHidden(walletId, tokenAddress, true)
            }

            val (setupDismissed, hasOpenedTelegramChannel) = rnLegacy.getSetup(walletId)

            if (hasOpenedTelegramChannel) {
                walletPrefsFolder.setTelegramChannel(walletId)
            }
            if (setupDismissed) {
                walletPrefsFolder.setupHide(walletId)
            }

            val spamTransactions = rnLegacy.getSpamTransactions(walletId)
            for (transactionId in spamTransactions.spam) {
                walletPrefsFolder.setSpamStateTransaction(walletId, transactionId, SpamTransactionState.SPAM)
            }

            for (transactionId in spamTransactions.nonSpam) {
                walletPrefsFolder.setSpamStateTransaction(walletId, transactionId, SpamTransactionState.NOT_SPAM)
            }
        }
    }

    private fun getLocales(): LocaleListCompat {
        val code = language.code
        return if (code == Language.DEFAULT) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(code)
        }
    }

    private fun fixCountryCode(code: String?): String {
        return if (code.isNullOrBlank()) {
            getLocale().country
        } else {
            code
        }
    }

}