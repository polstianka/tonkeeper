package com.tonapps.wallet.data.core.api

import android.util.Log
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

abstract class RemoteApiDataSource<Req: Request, Res> {
    abstract suspend fun requestImpl (request: Req): Res

    data class Status<Res>(
        internal val deferred: Deferred<Res?>?,
        val lastSuccessfulUpdate: Long,
        val lastUpdateIsFailed: Boolean,
    ) {
        val isLoading: Boolean get() = deferred != null
    }

    private val requestsStatus = HashMap<String, Status<Res>>()

    fun status(key: String): Status<Res> {
        return requestsStatus[key] ?: Status(deferred = null, lastSuccessfulUpdate = 0, lastUpdateIsFailed = false)
    }

    suspend fun request(request: Req): Res? = withContext(Dispatchers.IO) {
        val key = request.requestKey()
        val status = status(key)

        val existingRequest = status.deferred
        if (existingRequest != null) {
            return@withContext existingRequest.await()
        } else {
            Log.i("EMIT_REQUEST", "REQUEST_" + key)
            val newRequest = async {
                try {
                    requestImpl(request)
                } catch (t: Throwable) {
                    Log.i("REQUEST_ERROR", t.toString())
                    null
                }
            }

            requestsStatus[key] = status.copy(deferred = newRequest)
            try {
                val result = newRequest.await()
                requestsStatus[key] = status.copy(
                    deferred = null,
                    lastUpdateIsFailed = false,
                    lastSuccessfulUpdate = System.currentTimeMillis()
                )

                return@withContext result
            } catch (t: Throwable) {
                requestsStatus[key] = status.copy(
                    deferred = null,
                    lastUpdateIsFailed = true,
                )
            }
        }

        return@withContext null
    }
}