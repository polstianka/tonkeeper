package com.tonapps.wallet.data.events

import android.content.Context
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.collectibles.CollectiblesRepository
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.events.entities.EventEntity
import com.tonapps.wallet.data.events.source.LocalDataSource
import com.tonapps.wallet.data.events.source.RemoteDataSource
import com.tonapps.wallet.data.rates.entity.RatesEntity
import io.tonapi.models.AccountEvents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventsRepository(
    private val context: Context,
    private val api: API
) {

    private val localDataSource = LocalDataSource(context)
    private val remoteDataSource = RemoteDataSource(api)
    private val decryptedComments = context.getSharedPreferences("events.decrypted_comments", Context.MODE_PRIVATE)

    private fun decryptedCommentKey(txId: String): String {
        return "tx_$txId"
    }

    fun getDecryptedComment(txId: String): String? {
        return decryptedComments.getString(decryptedCommentKey(txId), null)
    }

    fun setDecryptedComment(txId: String, comment: String) {
        decryptedComments.edit().putString(decryptedCommentKey(txId), comment).apply()
    }

    suspend fun getLast(
        accountId: String,
        testnet: Boolean
    ): AccountEvents? = withContext(Dispatchers.IO) {
        try {
            remoteDataSource.get(accountId, testnet, limit = 2)
        } catch (e: Throwable) {
            null
        }
    }

    suspend fun loadForToken(
        tokenAddress: String,
        accountId: String,
        testnet: Boolean,
        beforeLt: Long? = null
    ): AccountEvents? = withContext(Dispatchers.IO) {
        if (tokenAddress == TokenEntity.TON.address) {
            getRemote(accountId, testnet, beforeLt)
        } else {
            try {
                api.getTokenEvents(tokenAddress, accountId, testnet, beforeLt)
            } catch (e: Throwable) {
                null
            }
        }
    }

    suspend fun getRemote(
        accountId: String,
        testnet: Boolean,
        beforeLt: Long? = null,
    ): AccountEvents? = withContext(Dispatchers.IO) {
        try {
            if (beforeLt != null) {
                remoteDataSource.get(accountId, testnet, beforeLt)
            } else {
                val events = remoteDataSource.get(accountId, testnet)
                localDataSource.setCache(cacheKey(accountId, testnet), events)
                events
            }
        } catch (e: Throwable) {
            null
        }
    }

    suspend fun getLocal(
        accountId: String,
        testnet: Boolean
    ): AccountEvents? = withContext(Dispatchers.IO) {
        localDataSource.getCache(cacheKey(accountId, testnet))
    }

    private fun cacheKey(accountId: String, testnet: Boolean): String {
        if (!testnet) {
            return accountId
        }
        return "${accountId}_testnet"
    }
}