package com.tonapps.wallet.data.settings

import android.content.Context
import android.icu.util.Currency
import android.util.Log
import androidx.core.content.edit
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
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
        private const val BATTERY_VIEWED_KEY = "battery_viewed"
        private const val CHART_PERIOD_KEY = "chart_period"
        private const val SAFE_MODE_DISABLED_UNIX_KEY = "safe_mode_disabled_unix"
        private const val SHOW_SAFE_MODE_SETUP_KEY = "show_safe_mode_setup"
        private const val ADDRESS_COPY_COUNT_KEY = "address_copy_count"
        private const val STORIES_VIEWED_PREFIX = "stories_viewed_"
        private const val CARDS_DISMISSED_KEY = "cards_dismissed"
    }

    private val _currencyFlow = MutableEffectFlow<WalletCurrency>()
    val currencyFlow = _currencyFlow.stateIn(scope, SharingStarted.Eagerly, null).filterNotNull().distinctUntilChanged()

    private val _languageFlow = MutableEffectFlow<Language>()
    val languageFlow = _languageFlow.stateIn(scope, SharingStarted.Eagerly, null).filterNotNull()

    private val _hiddenBalancesFlow = MutableEffectFlow<Boolean>()
    val hiddenBalancesFlow = _hiddenBalancesFlow.stateIn(scope, SharingStarted.Eagerly, null).filterNotNull()

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

    private val _safeModeStateFlow = MutableStateFlow<SafeModeState?>(null)
    val safeModeStateFlow = _safeModeStateFlow.asStateFlow().filterNotNull()

    private val _isMigratedFlow = MutableStateFlow<Boolean?>(null)
    val isMigratedFlow = _isMigratedFlow.asStateFlow().filterNotNull()

    private val _cardsDismissedFlow = MutableStateFlow<Boolean?>(null)
    val cardsDismissedFlow = _cardsDismissedFlow.asStateFlow().filterNotNull()

    fun notifyWalletPush() {
        _walletPush.tryEmit(Unit)
    }

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
                migrationHelper.setLegacyTheme(value)
            }
        }

    var chartPeriod: ChartPeriod = ChartPeriod.of(prefs.getString(CHART_PERIOD_KEY, ""))
        set(value) {
            if (value != field) {
                prefs.edit().putString(CHART_PERIOD_KEY, value.value).apply()
                field = value
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

    var language: Language = Language(prefs.getString(LANGUAGE_CODE_KEY, Language.DEFAULT) ?: Language.DEFAULT)
        set(value) {
            if (value != field) {
                field = value
                prefs.edit().putString(LANGUAGE_CODE_KEY, field.code).apply()
                _languageFlow.tryEmit(field)
                migrationHelper.setLegacyLanguage(value)
            }
        }

    val localeList: LocaleListCompat
        get() {
            if (language.code.isBlank() || language.code == Language.DEFAULT) {
                return LocaleListCompat.getEmptyLocaleList()
            } else {
                val newLocale = Locale.forLanguageTag(language.code)
                return LocaleListCompat.create(newLocale)
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

    var batteryViewed: Boolean = prefs.getBoolean(BATTERY_VIEWED_KEY, false)
        set(value) {
            if (value != field) {
                prefs.edit().putBoolean(BATTERY_VIEWED_KEY, value).apply()
                field = value
            }
        }

    var cardsDismissed: Boolean = prefs.getBoolean(CARDS_DISMISSED_KEY, false)
        set(value) {
            if (value != field) {
                prefs.edit().putBoolean(CARDS_DISMISSED_KEY, value).apply()
                field = value
                _cardsDismissedFlow.tryEmit(value)
            }
        }

    var showSafeModeSetup: Boolean = prefs.getBoolean(SHOW_SAFE_MODE_SETUP_KEY, false)
        set(value) {
            if (value != field) {
                prefs.edit().putBoolean(SHOW_SAFE_MODE_SETUP_KEY, value).apply()
                field = value
            }
        }

    val addressCopyCount: Int
        get() = prefs.getInt(ADDRESS_COPY_COUNT_KEY, 0)

    fun getSafeModeState(): SafeModeState {
        val disabledUnix = prefs.getLong(SAFE_MODE_DISABLED_UNIX_KEY, -5)
        if (disabledUnix == -5L) {
            return SafeModeState.Default
        } else if (1L == disabledUnix) {
            return SafeModeState.Enabled
        } else if (0 >= disabledUnix) {
            return SafeModeState.DisabledPermanently
        }
        // Auto enabled after 24 hours
        val disableTimeout = disabledUnix + 24 * 60 * 60 * 1000
        val diff = disableTimeout - System.currentTimeMillis()
        return if (diff > 0) {
            SafeModeState.Disabled
        } else {
            SafeModeState.Enabled
        }
    }

    fun isStoriesViewed(storyId: String): Boolean {
        return prefs.getBoolean(STORIES_VIEWED_PREFIX + storyId, false)
    }

    fun setStoriesViewed(storyId: String) {
        prefs.edit().putBoolean(STORIES_VIEWED_PREFIX + storyId, true).apply()
    }

    fun setSafeModeState(state: SafeModeState) {
        prefs.edit {
            val value = when (state) {
                SafeModeState.Enabled -> 1
                SafeModeState.DisabledPermanently -> 0
                SafeModeState.Default -> -5
                else -> System.currentTimeMillis()
            }
            putLong(SAFE_MODE_DISABLED_UNIX_KEY, value)
        }

        _safeModeStateFlow.tryEmit(state)
    }

    fun isUSDTW5(walletId: String) = walletPrefsFolder.isUSDTW5(walletId)

    fun disableUSDTW5(walletId: String) = walletPrefsFolder.disableUSDTW5(walletId)

    fun getSpamStateTransaction(walletId: String, id: String) = walletPrefsFolder.getSpamStateTransaction(walletId, id)

    fun setSpamStateTransaction(walletId: String, id: String, state: SpamTransactionState) = walletPrefsFolder.setSpamStateTransaction(walletId, id, state)

    fun isSpamTransaction(walletId: String, id: String) = getSpamStateTransaction(walletId, id) == SpamTransactionState.SPAM

    fun isPurchaseOpenConfirm(walletId: String, id: String) = walletPrefsFolder.isPurchaseOpenConfirm(walletId, id)

    fun disablePurchaseOpenConfirm(walletId: String, id: String) = walletPrefsFolder.disablePurchaseOpenConfirm(walletId, id)

    fun getPushWallet(walletId: String): Boolean = walletPrefsFolder.isPushEnabled(walletId)

    fun incrementCopyCount() {
        val count = addressCopyCount + 1
        prefs.edit().putInt(ADDRESS_COPY_COUNT_KEY, count).apply()
        walletPrefsFolder.notifyChanged()
    }

    suspend fun setPushWallet(walletId: String, value: Boolean) {
        walletPrefsFolder.setPushEnabled(walletId, value)
        rnLegacy.setNotificationsEnabled(walletId, value)
        _walletPush.tryEmit(Unit)
    }

    fun isSetupHidden(walletId: String): Boolean = walletPrefsFolder.isSetupHidden(walletId)

    fun setupHide(walletId: String) {
        walletPrefsFolder.setupHide(walletId)
        scope.launch {
            rnLegacy.setSetupDismissed(walletId)
        }
    }

    fun setTelegramChannel(walletId: String) {
        walletPrefsFolder.setTelegramChannel(walletId)
        scope.launch {
            rnLegacy.setHasOpenedTelegramChannel(walletId)
        }
    }

    fun isTelegramChannel(walletId: String) = walletPrefsFolder.isTelegramChannel(walletId)

    fun setLastBackupAt(walletId: String, date: Long) {
        scope.launch {
            rnLegacy.setSetupLastBackupAt(walletId, date)
        }
    }

    fun getBatteryTxEnabled(accountId: String) = walletPrefsFolder.getBatteryTxEnabled(accountId)

    fun setBatteryTxEnabled(accountId: String, types: Array<BatteryTransaction>) {
        walletPrefsFolder.setBatteryTxEnabled(accountId, types)
    }

    fun batteryEnableTx(
        accountId: String,
        type: BatteryTransaction,
        enable: Boolean
    ): Array<BatteryTransaction> {
        val types = getBatteryTxEnabled(accountId).toMutableSet()
        if (enable && !types.contains(type)) {
            types.add(type)
        } else if (!enable && !types.remove(type)) {
            return types.toTypedArray()
        }
        setBatteryTxEnabled(accountId, types.toTypedArray())
        return types.toTypedArray()
    }

    fun batteryIsEnabledTx(accountId: String, type: BatteryTransaction): Boolean {
        if (type == BatteryTransaction.UNKNOWN) {
            return false
        }
        return getBatteryTxEnabled(accountId).contains(type)
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
        setTokenState(walletId, tokenAddress, TokenPrefsEntity.State.NONE)
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

            _isMigratedFlow.value = true
            _currencyFlow.tryEmit(currency)
            _languageFlow.tryEmit(language)
            _hiddenBalancesFlow.tryEmit(hiddenBalances)
            _countryFlow.tryEmit(country)
            _searchEngineFlow.tryEmit(searchEngine)
            _biometricFlow.tryEmit(biometric)
            _lockscreenFlow.tryEmit(lockScreen)
            _safeModeStateFlow.tryEmit(getSafeModeState())
            _walletPush.tryEmit(Unit)
            _cardsDismissedFlow.tryEmit(cardsDismissed)
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

    private fun fixCountryCode(code: String?): String {
        return if (code.isNullOrBlank()) {
            getLocale().country
        } else {
            code
        }
    }

}