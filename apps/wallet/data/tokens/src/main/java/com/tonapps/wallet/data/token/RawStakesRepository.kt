package com.tonapps.wallet.data.token

import android.content.Context
import com.tonapps.extensions.toByteArray
import com.tonapps.extensions.toParcel
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.pool.PoolStakeEntity
import com.tonapps.wallet.data.core.BlobDataSource
import com.tonapps.wallet.data.core.api.ApiDataRepository
import com.tonapps.wallet.data.core.api.Repository
import com.tonapps.wallet.data.token.entities.StakesListEntity

class RawStakesRepository(
    context: Context,
    api: API,
) {
    private val source = RemoteDataSource(context, api)
    val storageFlow = source.storageFlow

    suspend fun doRequest(req: Request) = source.doRequest(req)

    suspend fun doRequest(accountId: String,  testnet: Boolean) =
        doRequest(Request(accountId = accountId, testnet = testnet))

    /* * */

    data class Request(
        val accountId: String,
        val testnet: Boolean
    ) : com.tonapps.wallet.data.core.api.Request {
        override fun cacheKey(): String {
            val key = "wallet_stakes_${accountId}"

            if (!testnet) {
                return key
            }
            return "${key}_testnet"
        }
        override fun requestKey(): String = cacheKey()
    }

    data class Storage(
        private val source: RemoteDataSource
    ): Repository<Request, StakesListEntity> {
        fun get( accountId: String, testnet: Boolean) = get(Request(accountId = accountId, testnet = testnet))
        override fun get(req: Request): ApiDataRepository.Result<StakesListEntity>? = source.get(req)
    }

    class RemoteDataSource(
        context: Context,
        private val api: API,
    ) : ApiDataRepository<Request, StakesListEntity, Storage>() {
        private val localDataSource = LocalDataSource(context)

        override fun storage(): Storage = Storage(this)

        override suspend fun requestImpl(request: Request): StakesListEntity {
            return StakesListEntity(list = api.getAccountNominatorsPools(request.accountId, request.testnet).sortedBy { it.pool }.map { PoolStakeEntity(it) })
        }

        override fun getCache(key: String): StakesListEntity? {
            return localDataSource.getCache(key)
        }

        override fun clearCache(key: String) {
            localDataSource.clearCache(key)
        }

        override fun setCache(key: String, value: StakesListEntity): Boolean {
            return localDataSource.updateCache(key, value)
        }
    }

    private class LocalDataSource(context: Context): BlobDataSource<StakesListEntity>(
        context = context,
        path = "wallet_stakes_2",
        lruInitialCapacity = 12
    ) {
        override fun onMarshall(data: StakesListEntity) = data.toByteArray()

        override fun onUnmarshall(bytes: ByteArray) = bytes.toParcel<StakesListEntity>()
    }
}