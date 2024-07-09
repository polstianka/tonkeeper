package com.tonapps.network

import android.net.Uri
import com.tonapps.network.interceptor.AuthorizationInterceptor
import okhttp3.OkHttpClient
import okhttp3.Request

object Network {

    @Deprecated("Use okHttpClient instead")
    val okHttpClient: OkHttpClient by lazy {
        okHttpBuilder().build()
    }
    fun newRequest(url: String) = Request.Builder().url(url)

    fun newRequest(uri: Uri) = newRequest(uri.toString())

    fun newCall(request: Request) = okHttpClient.newCall(request)

    fun request(request: Request) = newCall(request).execute()

    @Deprecated("Use okHttpClient instead")
    fun get(url: String): String {
        val request = newRequest(url).build()
        val response = newCall(request).execute()
        return response.body?.string()!!
    }

    private fun okHttpBuilder(): OkHttpClient.Builder {
        return OkHttpClient().newBuilder()
            .retryOnConnectionFailure(true)
            .addInterceptor(AuthorizationInterceptor.bearer("AF77F5JND26OLHQAAAAKQMSCYW3UVPFRA7CF2XHX6QG4M5WAMF5QRS24R7J4TF2UTSXOZEY"))
    }

}