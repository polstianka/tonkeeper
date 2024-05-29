package com.tonapps.network

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.retry
import com.tonapps.network.interceptor.AuthorizationInterceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import org.json.JSONObject

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

    fun post(url: String, body: RequestBody): String {
        val request = newRequest(url).post(body).build()
        val response = newCall(request).execute()
        return response.body?.string()!!
    }

    private fun okHttpBuilder(): OkHttpClient.Builder {
        return OkHttpClient().newBuilder()
            .retryOnConnectionFailure(true)
            .addInterceptor(AuthorizationInterceptor.bearer("AF77F5JND26OLHQAAAAKQMSCYW3UVPFRA7CF2XHX6QG4M5WAMF5QRS24R7J4TF2UTSXOZEY"))
    }

}