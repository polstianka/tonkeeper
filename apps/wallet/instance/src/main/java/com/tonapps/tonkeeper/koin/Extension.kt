package com.tonapps.tonkeeper.koin

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.ConfigEntity
import com.tonapps.wallet.api.entity.FlagsEntity
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.tonconnect.TonConnectRepository
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

val Context.koin: Koin?
    get() = (applicationContext as? KoinComponent)?.getKoin()

val Context.walletRepository: WalletRepository?
    get() = koin?.get<WalletRepository>()

val Context.api: API?
    get() = koin?.get<API>()

val Context.remoteConfig: ConfigEntity?
    get() = api?.config

val Context.settingsRepository: SettingsRepository?
    get() = koin?.get<SettingsRepository>()

val Context.tonConnectRepository: TonConnectRepository?
    get() = koin?.get<TonConnectRepository>()