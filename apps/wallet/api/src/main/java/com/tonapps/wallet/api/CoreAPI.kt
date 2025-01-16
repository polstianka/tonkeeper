package com.tonapps.wallet.api

import android.content.Context
import android.os.Build
import com.google.android.gms.net.CronetProviderInstaller
import com.google.firebase.crashlytics.BuildConfig
import com.tonapps.extensions.appVersionName
import com.tonapps.extensions.cacheFolder
import com.tonapps.extensions.locale
import com.tonapps.network.interceptor.AcceptLanguageInterceptor
import com.tonapps.network.interceptor.AuthorizationInterceptor
import com.tonapps.wallet.api.cronet.CronetInterceptor
import com.tonapps.wallet.api.entity.ConfigEntity
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.chromium.net.CronetEngine
import java.util.concurrent.TimeUnit

abstract class CoreAPI(private val context: Context) {

    val appVersionName = context.appVersionName

    private val userAgent = "Tonkeeper/${appVersionName} (Linux; Android ${Build.VERSION.RELEASE}; ${Build.MODEL})"

    private var cronetEngine: CronetEngine? = null

    val defaultHttpClient = baseOkHttpClientBuilder(
        cronetEngine = { cronetEngine },
        timeoutSeconds = 15,
        interceptors = listOf(
            UserAgentInterceptor(userAgent),
        )
    ).build()

    val seeHttpClient = baseOkHttpClientBuilder(
        cronetEngine = { null },
        timeoutSeconds = 30,
        interceptors = listOf(
            UserAgentInterceptor(userAgent),
        )
    ).build()

    val wsHttpClient = baseOkHttpClientBuilder(
        cronetEngine = { null },
        timeoutSeconds = 30,
        interceptors = listOf(
            UserAgentInterceptor(userAgent),
        )
    ).build()

    init {
        if (!BuildConfig.DEBUG) {
            requestCronet(context, userAgent) {
                cronetEngine = it
            }
        }
    }

    fun tonAPIHttpClient(config: () -> ConfigEntity): OkHttpClient {
        return createTonAPIHttpClient(
            context = context,
            userAgent = userAgent,
            tonApiV2Key = { config().tonApiV2Key },
            allowDomains = { config().domains },
            cronetEngine = { cronetEngine }
        )
    }

    private companion object {

        class UserAgentInterceptor(private val userAgent: String) : Interceptor {
            override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", userAgent)
                    .build()
                return chain.proceed(request)
            }
        }

        private fun baseOkHttpClientBuilder(
            cronetEngine: () -> CronetEngine?,
            timeoutSeconds: Long = 5,
            interceptors: List<Interceptor> = emptyList()
        ): OkHttpClient.Builder {
            val builder = OkHttpClient().newBuilder()
                .retryOnConnectionFailure(true)
                .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .callTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .pingInterval(timeoutSeconds, TimeUnit.SECONDS)
                .followSslRedirects(true)
                .followRedirects(true)

            for (interceptor in interceptors) {
                builder.addInterceptor(interceptor)
            }

            builder.addInterceptor { chain ->
                cronetEngine()?.let { engine ->
                    CronetInterceptor.newBuilder(engine).build()
                }?.intercept(chain) ?: chain.proceed(chain.request())
            }

            return builder
        }

        private fun createTonAPIHttpClient(
            userAgent: String,
            context: Context,
            cronetEngine: () -> CronetEngine?,
            tonApiV2Key: () -> String,
            allowDomains: () -> List<String>
        ): OkHttpClient {
            val interceptors = listOf(
                UserAgentInterceptor(userAgent),
                AcceptLanguageInterceptor(context.locale),
                AuthorizationInterceptor.bearer(
                    token = tonApiV2Key,
                    allowDomains = allowDomains
                )
            )

            return baseOkHttpClientBuilder(
                cronetEngine = cronetEngine,
                interceptors = interceptors
            ).build()
        }

        private fun requestCronet(context: Context, userAgent: String, callback: (CronetEngine) -> Unit) {
            CronetProviderInstaller.installProvider(context).addOnSuccessListener {
                build(context, userAgent)?.let(callback)
            }
        }

        private fun build(context: Context, userAgent: String): CronetEngine? {
            if (!CronetProviderInstaller.isInstalled()) {
                return null
            }
            return try {
                CronetEngine.Builder(context)
                    .setUserAgent(userAgent)
                    .enableQuic(true)
                    .enableHttp2(true)
                    .enableBrotli(true)
                    .setStoragePath(context.cacheFolder("cronet").absolutePath)
                    .enableHttpCache(CronetEngine.Builder.HTTP_CACHE_DISK, 500 * 1024 * 1024)
                    .build()
            } catch (e: Throwable) {
                null
            }
        }
    }
}