package com.tonapps.tonkeeper.koin

import com.tonapps.network.NetworkMonitor
import com.tonapps.tonkeeper.billing.BillingManager
import com.tonapps.tonkeeper.core.history.HistoryHelper
import com.tonapps.tonkeeper.ui.screen.main.MainViewModel
import com.tonapps.tonkeeper.ui.screen.root.RootViewModel
import com.tonapps.tonkeeper.fragment.tonconnect.auth.TCAuthViewModel
import com.tonapps.tonkeeper.sign.SignManager
import com.tonapps.tonkeeper.ui.screen.action.ActionViewModel
import com.tonapps.tonkeeper.ui.screen.backup.main.BackupViewModel
import com.tonapps.tonkeeper.ui.screen.backup.check.BackupCheckViewModel
import com.tonapps.tonkeeper.ui.screen.battery.BatteryViewModel
import com.tonapps.tonkeeper.ui.screen.battery.refill.BatteryRefillViewModel
import com.tonapps.tonkeeper.ui.screen.battery.settings.BatterySettingsViewModel
import com.tonapps.tonkeeper.ui.screen.browser.connected.BrowserConnectedViewModel
import com.tonapps.tonkeeper.ui.screen.browser.dapp.DAppViewModel
import com.tonapps.tonkeeper.ui.screen.browser.explore.BrowserExploreViewModel
import com.tonapps.tonkeeper.ui.screen.browser.main.BrowserMainViewModel
import com.tonapps.tonkeeper.ui.screen.browser.search.BrowserSearchViewModel
import com.tonapps.tonkeeper.ui.screen.purchase.main.PurchaseViewModel
import com.tonapps.tonkeeper.ui.screen.collectibles.CollectiblesViewModel
import com.tonapps.tonkeeper.ui.screen.country.CountryPickerViewModel
import com.tonapps.tonkeeper.ui.screen.events.EventsViewModel
import com.tonapps.tonkeeper.ui.screen.settings.currency.CurrencyViewModel
import com.tonapps.tonkeeper.ui.screen.init.InitViewModel
import com.tonapps.tonkeeper.ui.screen.ledger.steps.LedgerConnectionViewModel
import com.tonapps.tonkeeper.ui.screen.settings.language.LanguageViewModel
import com.tonapps.tonkeeper.ui.screen.name.base.NameViewModel
import com.tonapps.tonkeeper.ui.screen.name.edit.EditNameViewModel
import com.tonapps.tonkeeper.ui.screen.nft.NftViewModel
import com.tonapps.tonkeeper.ui.screen.notifications.enable.NotificationsEnableViewModel
import com.tonapps.tonkeeper.ui.screen.notifications.manage.NotificationsManageViewModel
import com.tonapps.tonkeeper.ui.screen.send.contacts.SendContactsViewModel
import com.tonapps.tonkeeper.ui.screen.send.main.SendViewModel
import com.tonapps.tonkeeper.ui.screen.wallet.picker.PickerViewModel
import com.tonapps.tonkeeper.ui.screen.wallet.picker.list.WalletPickerAdapter
import com.tonapps.tonkeeper.ui.screen.settings.main.SettingsViewModel
import com.tonapps.tonkeeper.ui.screen.settings.passcode.ChangePasscodeViewModel
import com.tonapps.tonkeeper.ui.screen.settings.security.SecurityViewModel
import com.tonapps.tonkeeper.ui.screen.settings.theme.ThemeViewModel
import com.tonapps.tonkeeper.ui.screen.staking.stake.StakingViewModel
import com.tonapps.tonkeeper.ui.screen.staking.unstake.UnStakeViewModel
import com.tonapps.tonkeeper.ui.screen.staking.viewer.StakeViewerViewModel
import com.tonapps.tonkeeper.ui.screen.token.picker.TokenPickerViewModel
import com.tonapps.tonkeeper.ui.screen.token.viewer.TokenViewModel
import com.tonapps.tonkeeper.ui.screen.w5.stories.W5StoriesViewModel
import com.tonapps.tonkeeper.ui.screen.wallet.main.WalletViewModel
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.WalletAdapter
import com.tonapps.tonkeeper.ui.screen.wallet.manage.TokensManageViewModel
import com.tonapps.wallet.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val koinModel = module {
    factory { Dispatchers.Default }
    single(createdAtStart = true) { CoroutineScope(Dispatchers.IO + SupervisorJob()) }
    single { SettingsRepository(get(), get(), get()) }
    single { NetworkMonitor(get(), get()) }
    single { SignManager(get(), get(), get(), get(), get(), get()) }
    single { HistoryHelper(get(), get(), get(), get(), get(), get(), get()) }
    singleOf(::BillingManager)

    factory { (viewModel: com.tonapps.tonkeeper.ui.base.BaseWalletVM) ->
        // TODO
    }

    uiAdapter { WalletAdapter(get()) }
    uiAdapter { WalletPickerAdapter() }

    viewModel { parameters -> NameViewModel(mode = parameters.get(), get(), get(), get()) }
    viewModel { parameters -> InitViewModel(get(), get(), parameters.get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { MainViewModel(get(), get()) }
    viewModel { RootViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { PickerViewModel(get(), get()) }
    viewModel { WalletViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { CurrencyViewModel(get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { EditNameViewModel(get()) }
    viewModel { LanguageViewModel(get()) }
    viewModel { SecurityViewModel(get(), get(), get()) }
    viewModel { ThemeViewModel(get()) }
    viewModel { EventsViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { parameters -> TCAuthViewModel(request = parameters.get(), get(), get(), get()) }
    viewModel { CollectiblesViewModel(get(), get(), get(), get(), get()) }
    viewModel { parameters -> ActionViewModel(get(), args = parameters.get(), get(), get(), get(), get()) }
    viewModel { BrowserExploreViewModel(get(), get(), get(), get(), get()) }
    viewModel { BrowserConnectedViewModel(get(), get()) }
    viewModel { BrowserMainViewModel(get(), get()) }
    viewModel { BrowserSearchViewModel(get(), get(), get(), get()) }
    viewModel { parameters -> DAppViewModel(get(), url = parameters.get(), get(), get()) }
    viewModel { ChangePasscodeViewModel(get(), get()) }
    viewModel { NotificationsManageViewModel(get(), get(), get()) }
    viewModel { parameters -> TokenViewModel(get(), tokenAddress = parameters.get(), get(), get(), get(), get(), get(), get()) }
    viewModel { BackupViewModel(get(), get(), get(), get()) }
    viewModel { BackupCheckViewModel(get(), get()) }
    viewModel { TokensManageViewModel(get(), get(), get(), get()) }
    viewModel { parameters -> SendViewModel(get(), nftAddress = parameters.get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { TokenPickerViewModel(get(), get(), get()) }
    viewModel { CountryPickerViewModel(get(), get(), get()) }
    viewModel { parameters -> StakingViewModel(address = parameters.get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { LedgerConnectionViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { W5StoriesViewModel(get(), get(), get(), get()) }
    viewModel { PurchaseViewModel(get(), get(), get()) }
    viewModel { parameters -> NftViewModel(nft = parameters.get(), get(), get(), get()) }
    viewModel { parameters -> StakeViewerViewModel(address = parameters.get(), get(), get(), get(), get()) }
    viewModel { parameters -> UnStakeViewModel(address = parameters.get(), get(), get(), get(), get(), get(), get()) }
    viewModel { SendContactsViewModel(get(), get(), get(), get()) }
    viewModel { NotificationsEnableViewModel(get(), get()) }
    viewModel { BatteryViewModel(androidApplication()) }
    viewModel { BatterySettingsViewModel(androidApplication(), get(), get(), get(), get()) }
    viewModel { BatteryRefillViewModel(androidApplication(), get(), get(), get(), get(), get(), get()) }
}