package com.tonapps.tonkeeper.ui.screen.settings.main

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.ton.contract.BaseWalletContract
import com.tonapps.blockchain.ton.contract.WalletVersion
import com.tonapps.blockchain.ton.extensions.toAccountId
import com.tonapps.extensions.appVersionCode
import com.tonapps.extensions.toUriOrNull
import com.tonapps.tonkeeper.Environment
import com.tonapps.tonkeeper.core.AnalyticsHelper
import com.tonapps.tonkeeper.extensions.capitalized
import com.tonapps.tonkeeper.manager.push.PushManager
import com.tonapps.tonkeeper.manager.tonconnect.TonConnectManager
import com.tonapps.tonkeeper.manager.widget.WidgetManager
import com.tonapps.tonkeeper.ui.base.BaseWalletVM
import com.tonapps.tonkeeper.ui.screen.card.entity.CardsStateEntity
import com.tonapps.tonkeeper.ui.screen.settings.main.list.Item
import com.tonapps.tonkeeper.usecase.sign.SignUseCase
import com.tonapps.tonkeeper.worker.PushToggleWorker
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.AccountRepository
import com.tonapps.wallet.data.account.Wallet
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.backup.BackupRepository
import com.tonapps.wallet.data.cards.CardsRepository
import com.tonapps.wallet.data.core.SearchEngine
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.passcode.PasscodeManager
import com.tonapps.wallet.data.rn.RNLegacy
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.localization.Language
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    application: Application,
    private val wallet: WalletEntity,
    private val accountRepository: AccountRepository,
    private val settingsRepository: SettingsRepository,
    private val api: API,
    private val backupRepository: BackupRepository,
    private val tonConnectManager: TonConnectManager,
    private val passcodeManager: PasscodeManager,
    private val rnLegacy: RNLegacy,
    private val environment: Environment,
    private val cardsRepository: CardsRepository,
    private val signUseCase: SignUseCase
): BaseWalletVM(application) {

    private val _uiItemsFlow = MutableStateFlow<List<Item>>(emptyList())
    val uiItemsFlow = _uiItemsFlow.asStateFlow().filter { it.isNotEmpty() }

    init {
        combine(
            settingsRepository.currencyFlow,
            settingsRepository.languageFlow,
            settingsRepository.searchEngineFlow,
            backupRepository.stream,
            accountRepository.selectedWalletFlow,
        ) { currency, language, searchEngine, backups, wallet ->
            val hasBackup = backups.indexOfFirst { it.walletId == wallet.id } > -1
            buildUiItems(wallet, currency, language, searchEngine, hasBackup)
        }.launchIn(viewModelScope)
    }

    fun setSearchEngine(searchEngine: SearchEngine?) {
        settingsRepository.searchEngine = searchEngine ?: SearchEngine.GOOGLE
    }

    fun signOut(callback: () -> Unit) {
        AnalyticsHelper.trackEvent("delete_wallet", settingsRepository.installId)
        viewModelScope.launch(Dispatchers.IO) {
            tonConnectManager.clear(wallet)
            PushToggleWorker.run(context, wallet, PushManager.State.Delete)
            delay(2000)
            withContext(Dispatchers.Main) {
                callback()
            }
            accountRepository.delete(wallet)
        }
    }

    fun createV4R2Wallet() {
        viewModelScope.launch(Dispatchers.IO) {
            val passcode = withContext(Dispatchers.Main) {
                passcodeManager.legacyGetPasscode(context)
            } ?: return@launch

            val newLabel = Wallet.Label(
                accountName = wallet.label.accountName + " V4R2",
                emoji = wallet.label.emoji,
                color = wallet.label.color
            )
            val walletId = AccountRepository.newWalletId()
            val version = WalletVersion.V4R2
            val mnemonic = accountRepository.getMnemonic(wallet.id)?.toList() ?: return@launch
            val walletIds = listOf(walletId)
            val versions = listOf(version)

            rnLegacy.addMnemonics(passcode, walletIds, mnemonic)
            accountRepository.importWallet(walletIds, Wallet.NewLabel(
                names = listOf(newLabel.name),
                emoji = newLabel.emoji,
                color = newLabel.color,
            ), mnemonic, versions, wallet.testnet, listOf(false))
            backupRepository.addBackup(walletId)
            accountRepository.setSelectedWallet(walletId)
            finish()
        }
    }

    private suspend fun hasW5(): Boolean {
        if (wallet.version == WalletVersion.V5R1) {
            return true
        } else if (wallet.type == Wallet.Type.Watch || wallet.type == Wallet.Type.Lockup || wallet.type == Wallet.Type.Ledger) {
            return true
        }
        val w5Contact = BaseWalletContract.create(wallet.publicKey, "v5r1", wallet.testnet)
        val accountId = w5Contact.address.toAccountId()
        return accountRepository.getWalletByAccountId(accountId, wallet.testnet) != null
    }

    private suspend fun hasV4R2(): Boolean {
        if (wallet.version != WalletVersion.V5R1 && wallet.version != WalletVersion.V5BETA) {
            return true
        }
        if (wallet.type == Wallet.Type.Watch || wallet.type == Wallet.Type.Lockup || wallet.type == Wallet.Type.Ledger) {
            return true
        }
        val v4R2Contact = BaseWalletContract.create(wallet.publicKey, "v4r2", wallet.testnet)
        val accountId = v4R2Contact.address.toAccountId()
        return accountRepository.getWalletByAccountId(accountId, wallet.testnet) != null
    }

    private suspend fun buildUiItems(
        displayWallet: WalletEntity,
        currency: WalletCurrency,
        language: Language,
        searchEngine: SearchEngine,
        hasBackup: Boolean
    ) {
        val hasW5 = hasW5()
        val hasV4R2 = hasV4R2()
        val uiItems = mutableListOf<Item>()
        uiItems.add(Item.Account(displayWallet))

        uiItems.add(Item.Space)
        if (wallet.hasPrivateKey) {
            uiItems.add(Item.Backup(ListCell.Position.FIRST, hasBackup))
            uiItems.add(Item.Security(ListCell.Position.LAST))
        } else {
            uiItems.add(Item.Security(ListCell.Position.SINGLE))
        }

        uiItems.add(Item.Space)
        uiItems.add(Item.Notifications(ListCell.Position.FIRST))

        if (wallet.hasPrivateKey) {
            if (!hasW5) {
                uiItems.add(Item.W5(ListCell.Position.MIDDLE))
            }
            if (!hasV4R2) {
                uiItems.add(Item.V4R2(ListCell.Position.MIDDLE))
            }
        }
        if (!wallet.testnet) {
            uiItems.add(Item.Currency(currency.code, ListCell.Position.MIDDLE))
        }

        if (wallet.isTonConnectSupported) {
            uiItems.add(Item.SearchEngine(searchEngine, ListCell.Position.MIDDLE))
            uiItems.add(Item.ConnectedApps(ListCell.Position.MIDDLE))
        }

        uiItems.add(Item.Language(language.nameLocalized.ifEmpty {
            getString(Localization.system)
        }.capitalized, ListCell.Position.MIDDLE))

        if (wallet.hasPrivateKey && !api.config.batteryDisabled) {
            uiItems.add(Item.Battery(ListCell.Position.MIDDLE))
        }
        if (wallet.isTonConnectSupported) {
            uiItems.add(Item.Cards(ListCell.Position.MIDDLE))
        }
        if (WidgetManager.isRequestPinAppWidgetSupported) {
            uiItems.add(Item.Widget(ListCell.Position.MIDDLE))
        }
        uiItems.add(Item.Theme(ListCell.Position.LAST))

        uiItems.add(Item.Space)
        uiItems.add(Item.FAQ(ListCell.Position.FIRST, api.config.faqUrl))
        uiItems.add(Item.Support(ListCell.Position.MIDDLE, getSupportUrl()))
        uiItems.add(Item.News(ListCell.Position.MIDDLE, api.config.tonkeeperNewsUrl))
        uiItems.add(Item.Contact(ListCell.Position.MIDDLE, api.config.supportLink))
        if (environment.isGooglePlayServicesAvailable) {
            uiItems.add(Item.Rate(ListCell.Position.MIDDLE))
        }
        uiItems.add(Item.Legal(ListCell.Position.LAST))

        uiItems.add(Item.Space)
        if (wallet.type == Wallet.Type.Watch) {
            uiItems.add(Item.DeleteWatchAccount(ListCell.Position.SINGLE))
        } else {
            uiItems.add(Item.Logout(ListCell.Position.SINGLE, wallet.label, !wallet.hasPrivateKey))
        }
        uiItems.add(Item.Space)
        uiItems.add(Item.Logo)

        _uiItemsFlow.value = uiItems
    }

    private fun getSupportUrl(): String {
        val startParams = "android${Build.VERSION.SDK_INT}app${context.appVersionCode}"
        val builder = api.config.directSupportUrl.toUri().buildUpon()
        builder.appendQueryParameter("start", startParams)
        return builder.toString()
    }

    private val cardsTokenFlow = flow {
        if (!wallet.isTonConnectSupported) {
            emit(null)
            return@flow
        }

        val storedToken = cardsRepository.getAccountToken(wallet.address, wallet.testnet)
        if (storedToken != null) {
            emit(storedToken)
        } else {
            // force prod endpoint
            val uri = api.getConfig(false).holdersAppEndpoint.toUriOrNull()
                ?: throw Exception("Invalid URL")
            val tonProof = signUseCase(context, wallet, uri.host!!, "ton-proof-any")
            val token = cardsRepository.fetchAccountToken(wallet.contract, tonProof, wallet.testnet)
            emit(token)
        }
    }.catch {
        emit(null)
    }.flowOn(Dispatchers.IO)

    val cardsStateFlow = cardsTokenFlow.map { token ->
        if (token != null) {
            CardsStateEntity(
                token = token,
                data = cardsRepository.getCachedData(wallet.address, wallet.testnet)
            )
        } else {
            null
        }
    }.catch {
        Log.d("WalletViewModel", "cardsUserStateFlow error: $it")
        emit(null)
    }.flowOn(Dispatchers.IO)
}