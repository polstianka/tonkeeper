package com.tonapps.network

import android.util.ArrayMap
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.internal.http2.ErrorCode
import okhttp3.internal.http2.StreamResetException
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.io.IOException

fun requestBuilder(url: String): Request.Builder {
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
    return execute(builder.build())
}

fun OkHttpClient.get(
    url: String,
    headers: ArrayMap<String, String>? = null
): String {
    return simple(url, headers).body?.string() ?: throw Exception("Empty response")
}

fun OkHttpClient.simple(
    url: String,
    headers: ArrayMap<String, String>? = null
): Response {
    val builder = requestBuilder(url)
    headers?.forEach { (key, value) ->
        builder.addHeader(key, value)
    }
    return execute(builder.build())
}

fun OkHttpClient.execute(request: Request): Response {
    val response = newCall(request).execute()
    if (!response.isSuccessful) {
        throw OkHttpError(response)
    }
    return response
}

class OkHttpError(
    private val response: Response
) : Exception("HTTP error: ${response.code}") {

    val statusCode: Int
        get() = response.code

    val body: String
        get() = response.body?.string() ?: ""
}

fun OkHttpClient.sseFactory() = EventSources.createFactory(this)

fun OkHttpClient.sse(
    url: String,
    lastEventId: Long? = null,
    onFailure: ((Throwable) -> Unit)?
): Flow<SSEvent> = callbackFlow {
    val listener = object : EventSourceListener() {
        override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
            if (!this@callbackFlow.trySend(SSEvent(id, type, data)).isSuccess) {
                eventSource.cancel()
            }
        }

        override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
            val error = when (t) {
                is StreamResetException -> t
                null -> IOException("SSE connection failed with response: ${response?.code}")
                else -> t
            }
            this@callbackFlow.cancel(CancellationException(error.message, error))
        }

        override fun onClosed(eventSource: EventSource) {
            this@callbackFlow.cancel(CancellationException("EventSource closed"))
        }
    }
    val builder = requestBuilder(url)
        .addHeader("Accept", "text/event-stream")
        .addHeader("Cache-Control", "no-cache")
        .addHeader("Connection", "keep-alive")
        .addHeader("Keep-Alive", "timeout=60")

    if (lastEventId != null) {
        builder.addHeader("Last-Event-ID", lastEventId.toString())
    }
    val request = builder.build()
    val events = sseFactory().newEventSource(request, listener)
    awaitClose {
        try {
            events.cancel()
        } catch (ignored: Exception) { }
    }
}.retry { cause ->
    when {
        cause is CancellationException -> false
        cause is StreamResetException && cause.errorCode == ErrorCode.CANCEL -> false
        cause is OutOfMemoryError -> false
        else -> {
            onFailure?.invoke(cause)
            delay(1000)
            true
        }
    }
}.cancellable()

fun OkHttpClient.webSocket(
    url: String,
    onFailure: ((Throwable) -> Unit)? = null,
    onOpen: ((WebSocket) -> Unit)? = null
): Pair<Flow<WebSocketEvent>, Channel<String>> {
    val sendChannel = Channel<String>(Channel.UNLIMITED)

    val request = Request.Builder()
        .url(url)
        .build()
    val flow = callbackFlow {
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "$url opened")
                onOpen?.invoke(webSocket)
                launch {
                    for (message in sendChannel) {
                        webSocket.send(message)
                    }
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "$url closed")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                this@callbackFlow.trySend(WebSocketEvent(data = text))
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.d("WebSocket", "$url failed", t)
                this@callbackFlow.close(t)
            }
        }

        val webSocket = newWebSocket(request, listener)
        awaitClose {
            Log.d("WebSocket", "$url closing")
            webSocket.close(1000, "Flow closed")
            sendChannel.close()
        }
    }.retry { cause ->
        when (cause) {
            is CancellationException -> false
            else -> {
                onFailure?.invoke(cause)
                delay(1000) // Retry delay
                true
            }
        }
    }.cancellable()

    return flow to sendChannel
}
