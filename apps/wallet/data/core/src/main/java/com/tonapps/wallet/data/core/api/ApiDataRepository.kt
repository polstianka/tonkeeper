package com.tonapps.wallet.data.core.api

import android.os.Parcelable
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

abstract class ApiDataRepository<Req: Request, Res: Parcelable, Storage: Repository<Req, Res>>: RemoteApiDataSource<Req, Res>() {
    private var seqno = 0
    private val _flow by lazy {
        MutableStateFlow(Seqno(value = storage(), seqno = seqno))
    }

    val storageFlow = _flow.asStateFlow().map { it.value }

    data class Seqno<T>(
        val value: T,
        val seqno: Int,
    )

    suspend fun doRequest(req: Req): Res? {
        val cacheKey = req.cacheKey()
        val result = request(req)

        if (result != null) {
            if (!setCache(cacheKey, result)) {
                return result
            }
        } else {
            getCache(cacheKey) ?: return null
        }

        emit(req.cacheKey())
        return result
    }

    fun emit(key: String) {
        Log.i("EMIT_UPDATE", "EMIT_UPDATE_" + key)

        seqno++
        _flow.value = Seqno(value = storage(), seqno = seqno)
    }

    fun get(req: Req): Result<Res>? {
        val key = req.cacheKey()
        val result = getCache(key)

        return result?.let { Result(status = status(key), result = it) }
    }

    data class Result<Res>(
        val status: Status<Res>,
        val result: Res
    )

    protected abstract fun storage(): Storage

    protected abstract fun getCache(key: String): Res?

    protected abstract fun setCache(key: String, value: Res): Boolean

    protected abstract fun clearCache(key: String)
}