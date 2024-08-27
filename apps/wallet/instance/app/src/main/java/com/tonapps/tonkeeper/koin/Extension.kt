package com.tonapps.tonkeeper.koin

import android.content.Context
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.core.history.HistoryHelper
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.ConfigEntity
import com.tonapps.wallet.data.account.AccountRepository
import com.tonapps.wallet.data.passcode.PasscodeManager
import com.tonapps.wallet.data.rn.RNLegacy
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.tonconnect.TonConnectRepository
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.definition.Definition
import org.koin.core.definition.KoinDefinition
import org.koin.core.module.Module

inline fun <reified T: RecyclerView.Adapter<*>> Module.uiAdapter(
    noinline definition: Definition<T>
): KoinDefinition<T> {
    return single(definition = definition)
}

@MainThread
inline fun <reified T : ViewModel> Fragment.parentFragmentViewModel(): Lazy<T> {
    return lazy { requireParentFragment().getViewModel() }
}

val Context.koin: Koin?
    get() = (applicationContext as? KoinComponent)?.getKoin()

val Context.accountRepository: AccountRepository?
    get() = koin?.get()

val Context.api: API?
    get() = koin?.get<API>()

val Context.remoteConfig: ConfigEntity?
    get() = api?.config

val Context.settingsRepository: SettingsRepository?
    get() = koin?.get<SettingsRepository>()

val Context.tonConnectRepository: TonConnectRepository?
    get() = koin?.get<TonConnectRepository>()

val Context.passcodeManager: PasscodeManager?
    get() = koin?.get<PasscodeManager>()

val Context.rnLegacy: RNLegacy?
    get() = koin?.get<RNLegacy>()

val Context.historyHelper: HistoryHelper?
    get() = koin?.get<HistoryHelper>()
