package com.tonapps.wallet.data.stonfi.api

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tonapps.wallet.data.stonfi.entities.StonfiAssetResponse
import com.tonapps.wallet.data.stonfi.entities.StonfiPairResponse
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

    @GET("v1/markets")
    suspend fun pairs(): StonfiPairResponse

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

        fun provideApi(context: Context): StonfiApi {
            val httpClient = OkHttpClient.Builder()
                .connectTimeout(40, TimeUnit.SECONDS)
                .readTimeout(40, TimeUnit.SECONDS)
                .writeTimeout(40, TimeUnit.SECONDS)


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