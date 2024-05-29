package com.tonapps.wallet.data.pools

import android.content.Context
import com.tonapps.extensions.toByteArray
import com.tonapps.extensions.toParcel
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.core.BlobDataSource
import com.tonapps.wallet.data.core.api.ApiDataRepository
import com.tonapps.wallet.data.core.api.Repository
import com.tonapps.wallet.data.pools.entities.ApyChartEntity
import com.tonapps.wallet.data.pools.entities.ApyChartPoint

class StakingHistoryRepository(
    context: Context,
    api: API,
) {
    private val source = RemoteDataSource(context, api)
    val storageFlow = source.storageFlow

    suspend fun doRequest(req: Request) {
        source.doRequest(req)
    }

    /* * */

    data class Request(
        val accountId: String,
        val testnet: Boolean
    ) : com.tonapps.wallet.data.core.api.Request {
        override fun cacheKey(): String {
            val key = "staking_pools_apy_history_${accountId}"

            if (!testnet) {
                return key
            }
            return "${key}_testnet"
        }

        override fun requestKey(): String = cacheKey()
    }

    data class Storage(
        private val source: RemoteDataSource
    ): Repository<Request, ApyChartEntity> {
        override fun get(req: Request): ApiDataRepository.Result<ApyChartEntity>? = source.get(req)
    }

    class RemoteDataSource(
        context: Context,
        private val api: API,
    ) : ApiDataRepository<Request, ApyChartEntity, Storage>() {
        private val localDataSource = LocalDataSource(context)

        override suspend fun requestImpl(request: Request): ApyChartEntity {
            val res = api.staking(request.testnet)
                .getStakingPoolHistory(request.accountId).apy.sortedBy { it.time }
            return ApyChartEntity(data = res.map { ApyChartPoint(it) })
        }

        override fun storage(): Storage {
            return Storage(this)
        }

        public override fun getCache(key: String): ApyChartEntity? {
            return localDataSource.getCache(key)
        }

        public override fun clearCache(key: String) {
            localDataSource.clearCache(key)
        }

        public override fun setCache(key: String, value: ApyChartEntity): Boolean {
            return localDataSource.updateCache(key, value)
        }
    }

    private class LocalDataSource(context: Context) : BlobDataSource<ApyChartEntity>(
        context = context,
        path = "staking_pools_apy_history",
        lruInitialCapacity = 25
    ) {
        override fun onMarshall(data: ApyChartEntity) = data.toByteArray()

        override fun onUnmarshall(bytes: ByteArray) = bytes.toParcel<ApyChartEntity>()
    }
}