package com.tonapps.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.ArrayMap
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.retry
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources

private fun requestBuilder(url: String): Request.Builder {
    val builder = Request.Builder()
    builder.url(url)
    return builder
}

fun OkHttpClient.postForm(
    url: String,
    formBody: FormBody,
    headers: ArrayMap<String, String>? = null
): Response {
    return post(url, formBody, headers)
}

fun OkHttpClient.postJSON(
    url: String,
    json: String,
    headers: ArrayMap<String, String>? = null
): Response {
    val body = json.toRequestBody("application/json".toMediaType())
    return post(url, body, headers)
}

fun OkHttpClient.post(
    url: String,
    body: RequestBody,
    headers: ArrayMap<String, String>? = null
): Response {
    val builder = requestBuilder(url)
    builder.post(body)
    headers?.forEach { (key, value) ->
        builder.addHeader(key, value)
    }
    return newCall(builder.build()).execute()
}

fun OkHttpClient.getRequest(
    url: String,
    headers: ArrayMap<String, String>? = null
): Response {
    val builder = requestBuilder(url)
    headers?.forEach { (key, value) ->
        builder.addHeader(key, value)
    }
    return newCall(builder.build()).execute()
}

fun OkHttpClient.get(
    url: String,
    headers: ArrayMap<String, String>? = null
): String {
    return getRequest(url, headers).body?.string() ?: throw Exception("Empty response")
}

fun OkHttpClient.getBitmap(url: String): Bitmap {
    val request = requestBuilder(url).build()
    val response = newCall(request).execute()
    return response.body?.byteStream()?.use { stream ->
        BitmapFactory.decodeStream(stream)
    } ?: throw Exception("Empty response")
}

fun OkHttpClient.sseFactory() = EventSources.createFactory(this)

fun OkHttpClient.sse(url: String): Flow<SSEvent> = callbackFlow {
    val listener = object : EventSourceListener() {
        override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
            this@callbackFlow.trySendBlocking(SSEvent(id, type, data))
        }

        override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
            this@callbackFlow.close(t)
        }

        override fun onClosed(eventSource: EventSource) {
            this@callbackFlow.close()
        }
    }
    val request = requestBuilder(url)
        .addHeader("Accept", "text/event-stream")
        .addHeader("Cache-Control", "no-cache")
        .addHeader("Connection", "keep-alive")
        .build()
    val events = sseFactory().newEventSource(request, listener)
    awaitClose { events.cancel() }
}.retry { _ ->
    delay(1000)
    true
}
