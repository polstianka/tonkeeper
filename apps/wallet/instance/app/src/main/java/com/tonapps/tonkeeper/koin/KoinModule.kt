package com.tonapps.tonkeeper.koin

import com.tonapps.network.NetworkMonitor
import com.tonapps.tonkeeper.Environment
import com.tonapps.tonkeeper.billing.BillingManager
import com.tonapps.tonkeeper.client.safemode.SafeModeClient
import com.tonapps.tonkeeper.manager.assets.AssetsManager
import com.tonapps.tonkeeper.manager.tx.TransactionManager
import com.tonapps.tonkeeper.core.history.HistoryHelper
import com.tonapps.tonkeeper.helper.CacheHelper
import com.tonapps.tonkeeper.manager.apk.APKManager
import com.tonapps.tonkeeper.manager.push.PushManager
import com.tonapps.tonkeeper.ui.screen.main.MainViewModel
import com.tonapps.tonkeeper.ui.screen.root.RootViewModel
import com.tonapps.tonkeeper.manager.tonconnect.TonConnectManager
import com.tonapps.tonkeeper.ui.base.BaseWalletVM
import com.tonapps.tonkeeper.ui.screen.add.AddWalletViewModel
import com.tonapps.tonkeeper.ui.screen.battery.BatteryViewModel
import com.tonapps.tonkeeper.ui.screen.browser.main.BrowserMainViewModel
import com.tonapps.tonkeeper.ui.screen.browser.search.BrowserSearchViewModel
import com.tonapps.tonkeeper.ui.screen.country.CountryPickerViewModel
import com.tonapps.tonkeeper.ui.screen.dev.DevViewModel
import com.tonapps.tonkeeper.ui.screen.external.ledger.LedgerConnectViewModel
import com.tonapps.tonkeeper.ui.screen.settings.currency.CurrencyViewModel
import com.tonapps.tonkeeper.ui.screen.init.InitViewModel
import com.tonapps.tonkeeper.ui.screen.ledger.steps.LedgerConnectionViewModel
import com.tonapps.tonkeeper.ui.screen.migration.MigrationViewModel
import com.tonapps.tonkeeper.ui.screen.settings.language.LanguageViewModel
import com.tonapps.tonkeeper.ui.screen.name.base.NameViewModel
import com.tonapps.tonkeeper.ui.screen.wallet.picker.PickerViewModel
import com.tonapps.tonkeeper.ui.screen.settings.passcode.ChangePasscodeViewModel
import com.tonapps.tonkeeper.ui.screen.settings.security.SecurityViewModel
import com.tonapps.tonkeeper.ui.screen.settings.theme.ThemeViewModel
import com.tonapps.tonkeeper.ui.screen.stories.w5.W5StoriesViewModel
import com.tonapps.tonkeeper.ui.screen.tonconnect.TonConnectViewModel
import com.tonapps.tonkeeper.usecase.emulation.EmulationUseCase
import com.tonapps.tonkeeper.usecase.sign.SignUseCase
import com.tonapps.wallet.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val koinModel = module {
    factory { Dispatchers.Default }

    single(createdAtStart = true) { CoroutineScope(Dispatchers.IO + SupervisorJob()) }
    singleOf(::Environment)

    singleOf(::SettingsRepository)
    singleOf(::NetworkMonitor)
    singleOf(::HistoryHelper)
    singleOf(::AssetsManager)
    singleOf(::BillingManager)
    singleOf(::TransactionManager)
    singleOf(::TonConnectManager)
    singleOf(::PushManager)
    singleOf(::SafeModeClient)
    singleOf(::APKManager)
    singleOf(::CacheHelper)

    factoryOf(::SignUseCase)
    factoryOf(::EmulationUseCase)

    viewModelOf(::DevViewModel)
    viewModelOf(::ChangePasscodeViewModel)
    viewModelOf(::CountryPickerViewModel)
    viewModelOf(::CurrencyViewModel)
    viewModelOf(::ThemeViewModel)
    viewModelOf(::LanguageViewModel)
    viewModelOf(::SecurityViewModel)
    viewModelOf(::BrowserMainViewModel)
    viewModelOf(::BrowserSearchViewModel)
    viewModelOf(::MigrationViewModel)

    viewModelOf(::NameViewModel)
    viewModelOf(::InitViewModel)
    viewModelOf(::MainViewModel)
    viewModelOf(::RootViewModel)
    viewModelOf(::PickerViewModel)
    viewModelOf(::TonConnectViewModel)


    viewModelOf(::LedgerConnectionViewModel)
    viewModelOf(::W5StoriesViewModel)
    viewModelOf(::AddWalletViewModel)
    viewModelOf(::BatteryViewModel)
    viewModelOf(BaseWalletVM::EmptyViewViewModel)
    viewModelOf(::LedgerConnectViewModel)
}