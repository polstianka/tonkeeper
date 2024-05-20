package com.tonapps.wallet.api

import android.content.Context
import com.tonapps.extensions.locale
import com.tonapps.network.interceptor.AcceptLanguageInterceptor
import com.tonapps.wallet.api.core.StonfiProvider
import com.tonapps.wallet.api.entity.ConfigEntity
import com.tonapps.wallet.api.internal.ConfigRepository
import com.tonapps.wallet.api.internal.InternalApi
import io.stonfiapi.apis.DexApi
import io.stonfiapi.apis.JettonApi
import io.stonfiapi.apis.StatsApi
import io.stonfiapi.apis.WalletsApi
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class StonfiAPI(
    private val context: Context,
    private val scope: CoroutineScope
) {

    companion object {
        private fun createStonfiAPIHttpClient(
            context: Context,
        ): OkHttpClient {
            return API.baseOkHttpClientBuilder()
                .addInterceptor(AcceptLanguageInterceptor(context.locale))
                .callTimeout(15L, TimeUnit.SECONDS)
                .readTimeout(15L, TimeUnit.SECONDS)
                .build()
        }
    }
    private val defaultHttpClient = API.baseOkHttpClientBuilder().build()
    private val internalApi = InternalApi(context, defaultHttpClient)
    private val configRepository = ConfigRepository(context, scope, internalApi)
    val config: ConfigEntity
        get() = configRepository.configEntity

    private val provider: StonfiProvider by lazy {
        StonfiProvider("https://api.ston.fi/", createStonfiAPIHttpClient(context))
    }

    val stats: StatsApi
        get() = provider.stats
    val dex: DexApi
        get() = provider.dex
    val wallets: WalletsApi
        get() = provider.wallets
    val jetton: JettonApi
        get() = provider.jetton
}