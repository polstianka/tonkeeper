package com.tonapps.wallet.data.stonfi.api

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tonapps.wallet.data.stonfi.entities.StonfiAssetResponse
import com.tonapps.wallet.data.stonfi.entities.StonfiSimulate
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.File
import java.util.concurrent.TimeUnit

interface StonfiApi {

    @GET("/v1/assets")
    suspend fun assets(): StonfiAssetResponse


    @POST("/v1/swap/simulate")
    suspend fun simulate(
        @Query("offer_address") offersAddress: String,
        @Query("ask_address") askAddress: String,
        @Query("units") units: String,
        @Query("slippage_tolerance") slippageTolerance: String,
    ): StonfiSimulate

    companion object {
        private fun provideCache(context: Context): Cache {
            return Cache(
                directory = File(context.cacheDir, "http-cache"),
                maxSize = 5L * 1024L * 1024L // 5 MiB
            )
        }

        private fun provideLogging(): HttpLoggingInterceptor {
            val logging = HttpLoggingInterceptor()
//            if (BuildConfig) {
//                //logging.setLevel(HttpLoggingInterceptor.Level.BODY)
//                logging.setLevel(HttpLoggingInterceptor.Level.NONE)
//            } else {
//                logging.setLevel(HttpLoggingInterceptor.Level.NONE)
//            }
            return logging
        }

        fun provideApi(context: Context): StonfiApi {
            val httpClient = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
//                .cache(provideCache(context))
//                .addInterceptor(provideLogging())


            return Retrofit.Builder()
                .client(httpClient.build())
                .baseUrl("https://api.ston.fi")
                .addConverterFactory(
                    MoshiConverterFactory.create(
                        Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                    )
                )
                .build().create(StonfiApi::class.java)

        }
    }
}