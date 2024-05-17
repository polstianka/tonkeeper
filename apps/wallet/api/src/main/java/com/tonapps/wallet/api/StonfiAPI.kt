package com.tonapps.wallet.api

import android.content.Context
import com.tonapps.extensions.locale
import com.tonapps.network.interceptor.AcceptLanguageInterceptor
import com.tonapps.wallet.api.core.StonfiProvider
import com.tonapps.wallet.api.entity.ConfigEntity
import com.tonapps.wallet.api.internal.ConfigRepository
import com.tonapps.wallet.api.internal.InternalApi
import io.stonfiapi.apis.StatsApi
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient

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
                .build()
        }
    }
    private val defaultHttpClient = API.baseOkHttpClientBuilder().build()
    private val internalApi = InternalApi(context, defaultHttpClient)
    private val configRepository = ConfigRepository(context, scope, internalApi)
    val config: ConfigEntity
        get() = configRepository.configEntity

    private val provider: StonfiProvider by lazy {
        StonfiProvider(config.stonfiUrl, createStonfiAPIHttpClient(context))
    }

    val stats: StatsApi
        get() = provider.stats
}