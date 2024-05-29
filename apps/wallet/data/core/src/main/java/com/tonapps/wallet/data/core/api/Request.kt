package com.tonapps.wallet.data.core.api

interface Request {
    fun cacheKey(): String
    fun requestKey(): String
}