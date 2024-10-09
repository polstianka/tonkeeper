package com.tonapps.wallet.api

import android.os.SystemClock
import io.infrastructure.Serializer
import com.squareup.moshi.adapter
import io.infrastructure.ClientException
import android.util.Log
import com.tonapps.network.OkHttpError
import kotlinx.coroutines.delay
import kotlinx.coroutines.CancellationException

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> toJSON(obj: T?): String {
    if (obj == null) {
        return ""
    }
    return io.infrastructure.Serializer.moshi.adapter<T>().toJson(obj)
}

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> fromJSON(json: String): T {
    return io.infrastructure.Serializer.moshi.adapter<T>().fromJson(json)!!
}

fun <R> withRetry(
    times: Int = 5,
    delay: Long = 300,
    retryBlock: () -> R
): R? {
    repeat(times) {
        try {
            return retryBlock()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            val statusCode = when (e) {
                is ClientException -> e.statusCode
                is OkHttpError -> e.statusCode
                else -> 0
            }
            if (statusCode == 429) { // Too many requests
                SystemClock.sleep((3000..5000).random().toLong())
                return withRetry(times, delay, retryBlock)
            } else if (statusCode >= 500 || statusCode == 404) {
                return null
            }
        }
        SystemClock.sleep(delay)
    }
    return null
}
