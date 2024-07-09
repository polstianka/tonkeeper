package com.tonapps.tonkeeper.ui.screen.swap

import android.content.Context
import android.util.Log
import com.tonapps.extensions.locale
import com.tonapps.network.get
import com.tonapps.network.interceptor.AcceptLanguageInterceptor
import com.tonapps.network.interceptor.AuthorizationInterceptor
import com.tonapps.tonkeeper.ui.screen.swap.model.assets.AssetsModel
import com.tonapps.wallet.api.API
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class SwapRepository(private val context: Context) {

    private val tonAPIHttpClient: OkHttpClient by lazy {
        createTonAPIHttpClient(context)
    }

    suspend fun getTokenFormAccount(address: String): AssetsModel? {
        try {
            val resultFromGet = tonAPIHttpClient.get("${BASIC_URL}v1/wallets/${address}/assets")
            val json = Json { ignoreUnknownKeys = true }
            val decodedResult = json.decodeFromString(AssetsModel.serializer(), resultFromGet)
            return if(decodedResult.asset_list != null) {
                val newList = decodedResult.asset_list.filter { it.dex_usd_price != null }
                val newAssetsModel = AssetsModel(newList)
                newAssetsModel
            }else {
                null
            }
        } catch (e: Exception) {
            Log.d("SwapRepository", "getTokenFormAccount - e ${e.stackTraceToString()}")
            return null
        }
    }

    companion object {

        const val BASIC_URL = "https://api.ston.fi/"

        val JSON = Json { prettyPrint = true }

        private fun baseOkHttpClientBuilder(): OkHttpClient.Builder {
            return OkHttpClient().newBuilder()
                .retryOnConnectionFailure(false)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .callTimeout(10, TimeUnit.SECONDS)
        }

        private fun createTonAPIHttpClient(
            context: Context,
        ): OkHttpClient {
            return baseOkHttpClientBuilder()
                .addInterceptor(AcceptLanguageInterceptor(context.locale))
                .build()
        }
    }
}