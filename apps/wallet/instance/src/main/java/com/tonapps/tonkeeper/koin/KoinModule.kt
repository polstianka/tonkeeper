package com.tonapps.tonkeeper.koin

import com.tonapps.network.NetworkMonitor
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.api.account.AccountRepository
import com.tonapps.tonkeeper.core.history.HistoryHelper
import com.tonapps.tonkeeper.fragment.chart.ChartScreenFeature
import com.tonapps.tonkeeper.fragment.jetton.JettonScreenFeature
import com.tonapps.tonkeeper.ui.screen.main.MainViewModel
import com.tonapps.tonkeeper.ui.screen.root.RootViewModel
import com.tonapps.tonkeeper.fragment.send.amount.AmountScreenFeature
import com.tonapps.tonkeeper.fragment.send.confirm.ConfirmScreenFeature
import com.tonapps.tonkeeper.fragment.send.recipient.RecipientScreenFeature
import com.tonapps.tonkeeper.fragment.signer.TransactionDataHelper
import com.tonapps.tonkeeper.fragment.stake.confirm.ConfirmStakeViewModel
import com.tonapps.tonkeeper.fragment.stake.pick_option.PickStakingOptionViewModel
import com.tonapps.tonkeeper.fragment.stake.pick_pool.PickPoolViewModel
import com.tonapps.tonkeeper.fragment.stake.pool_details.PoolDetailsViewModel
import com.tonapps.tonkeeper.fragment.stake.root.StakeViewModel
import com.tonapps.tonkeeper.fragment.swap.confirm.ConfirmSwapViewModel
import com.tonapps.tonkeeper.fragment.swap.pick_asset.PickAssetViewModel
import com.tonapps.tonkeeper.fragment.swap.root.SwapViewModel
import com.tonapps.tonkeeper.fragment.swap.settings.SwapSettingsViewModel
import com.tonapps.tonkeeper.fragment.tonconnect.auth.TCAuthViewModel
import com.tonapps.tonkeeper.fragment.trade.exchange.vm.ExchangeViewModel
import com.tonapps.tonkeeper.fragment.trade.pick_currency.PickCurrencyViewModel
import com.tonapps.tonkeeper.fragment.trade.pick_operator.PickOperatorViewModel
import com.tonapps.tonkeeper.fragment.trade.root.vm.BuySellViewModel
import com.tonapps.tonkeeper.password.PasscodeDataStore
import com.tonapps.tonkeeper.password.PasscodeRepository
import com.tonapps.wallet.data.push.PushManager
import com.tonapps.tonkeeper.sign.SignManager
import com.tonapps.tonkeeper.ui.screen.action.ActionViewModel
import com.tonapps.tonkeeper.ui.screen.browser.connected.BrowserConnectedViewModel
import com.tonapps.tonkeeper.ui.screen.browser.dapp.DAppViewModel
import com.tonapps.tonkeeper.ui.screen.browser.explore.BrowserExploreViewModel
import com.tonapps.tonkeeper.ui.screen.browser.main.BrowserMainViewModel
import com.tonapps.tonkeeper.ui.screen.browser.search.BrowserSearchViewModel
import com.tonapps.tonkeeper.ui.screen.collectibles.CollectiblesViewModel
import com.tonapps.tonkeeper.ui.screen.events.EventsViewModel
import com.tonapps.tonkeeper.ui.screen.settings.currency.CurrencyViewModel
import com.tonapps.tonkeeper.ui.screen.init.InitViewModel
import com.tonapps.tonkeeper.ui.screen.settings.language.LanguageViewModel
import com.tonapps.tonkeeper.ui.screen.name.base.NameViewModel
import com.tonapps.tonkeeper.ui.screen.name.edit.EditNameViewModel
import com.tonapps.tonkeeper.ui.screen.picker.PickerViewModel
import com.tonapps.tonkeeper.ui.screen.picker.list.WalletPickerAdapter
import com.tonapps.tonkeeper.ui.screen.settings.main.SettingsViewModel
import com.tonapps.tonkeeper.ui.screen.settings.security.SecurityViewModel
import com.tonapps.tonkeeper.ui.screen.settings.theme.ThemeViewModel
import com.tonapps.tonkeeper.ui.screen.wallet.WalletViewModel
import com.tonapps.tonkeeper.ui.screen.wallet.list.WalletAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val koinModel = module {
    factory { Dispatchers.Default }
    single(createdAtStart = true) { CoroutineScope(Dispatchers.IO + SupervisorJob()) }
    single { App.walletManager }
    single { App.settings }
    single { AccountRepository() }
    single { PasscodeDataStore(get()) }
    single { PasscodeRepository(get(), get()) }
    single { NetworkMonitor(get(), get()) }
    single { PushManager(get(), get(), get(), get(), get(), get(), get()) }
    single { SignManager(get(), get(), get(), get(), get()) }
    single { HistoryHelper(get(), get()) }

    uiAdapter { WalletAdapter(get()) }
    uiAdapter { WalletPickerAdapter() }
    factory { TransactionDataHelper(get()) }

    viewModel { parameters -> NameViewModel(mode = parameters.get(), get(), get()) }
    viewModel { parameters -> InitViewModel(parameters.get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { MainViewModel(get(), get()) }
    viewModel { RootViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { RecipientScreenFeature(get()) }
    viewModel { PickerViewModel(get()) }
    viewModel { WalletViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { ConfirmScreenFeature(get(), get(), get(), get(), get()) }
    viewModel { CurrencyViewModel(get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
    viewModel { EditNameViewModel(get()) }
    viewModel { LanguageViewModel(get()) }
    viewModel { SecurityViewModel(get(), get(), get()) }
    viewModel { ThemeViewModel(get()) }
    viewModel { EventsViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { parameters -> TCAuthViewModel(request = parameters.get(), get(), get(), get()) }
    viewModel { CollectiblesViewModel(get(), get(), get()) }
    viewModel { parameters -> ActionViewModel(args = parameters.get(), get(), get()) }
    viewModel { BrowserExploreViewModel(get(), get(), get(), get()) }
    viewModel { BrowserConnectedViewModel(get(), get()) }
    viewModel { BrowserMainViewModel(get()) }
    viewModel { BrowserSearchViewModel(get(), get(), get(), get()) }
    viewModel { DAppViewModel(get(), get()) }

    viewModel { ChartScreenFeature(get(), get(), get()) }
    viewModel { JettonScreenFeature(get(), get()) }
    viewModel { AmountScreenFeature(get(), get()) }
    viewModel { BuySellViewModel() }

    viewModel { ExchangeViewModel(get(), get(), get(), get()) }
    viewModel { PickOperatorViewModel(get(), get(), get()) }
    viewModel { PickCurrencyViewModel(get()) }

    viewModel { StakeViewModel(get(), get(), get(), get(), get()) }
    viewModel { PickStakingOptionViewModel() }
    viewModel { PickPoolViewModel() }
    viewModel { PoolDetailsViewModel() }
    viewModel { ConfirmStakeViewModel(get(), get(), get(), get(), get(), get(), get()) }

    viewModel { SwapViewModel(get(), get(), get()) }
    viewModel { PickAssetViewModel(get()) }
    viewModel { SwapSettingsViewModel() }
    viewModel { ConfirmSwapViewModel() }
}