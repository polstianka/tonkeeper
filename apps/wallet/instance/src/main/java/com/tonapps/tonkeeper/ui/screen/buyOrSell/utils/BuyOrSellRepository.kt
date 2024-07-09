package com.tonapps.tonkeeper.ui.screen.buyOrSell.utils

import android.content.Context
import android.util.Log
import com.tonapps.extensions.locale
import com.tonapps.network.get
import com.tonapps.network.interceptor.AcceptLanguageInterceptor
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.RatesModel.RatesModel
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.fiatModel.FiatModel
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class BuyOrSellRepository(private val context: Context) {
    private val tonAPIHttpClient: OkHttpClient by lazy {
        createTonAPIHttpClient(context)
    }


    suspend fun getRateBuy(currency: String): RatesModel? {
        try {
            val resultFromGet =
                tonAPIHttpClient.get("${BASIC_URL_RATE}buy/rates?currency=${currency}")
            val json = Json { ignoreUnknownKeys = true }
            val decodedResult = json.decodeFromString(RatesModel.serializer(), resultFromGet)

            return if (decodedResult.itemRates != null) {
                decodedResult
            } else {
                null
            }
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun getRateSell(currency: String): RatesModel? {
        try {
            val resultFromGet =
                tonAPIHttpClient.get("${BASIC_URL_RATE}sell/rates?currency=${currency}")
            val json = Json { ignoreUnknownKeys = true }
            val decodedResult = json.decodeFromString(RatesModel.serializer(), resultFromGet)

            return if (decodedResult.itemRates != null) {
                decodedResult
            } else {
                null
            }
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun getFiat(): FiatModel? {
        try {

            val resultFromGet = tonAPIHttpClient.get("https://api.tonkeeper.com/fiat/methods")
            val json = Json { ignoreUnknownKeys = true }
            val decodedResult = json.decodeFromString(FiatModel.serializer(), resultFromGet)

            return if (decodedResult.data != null) {
                decodedResult
            } else {
                null
            }
        } catch (e: Exception) {
            Log.d("getFiat", "e - ${e.stackTraceToString()}")
            return null
        }
    }

    companion object {

        const val BASIC_URL_RATE = "https://boot.tonkeeper.com/widget/"

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