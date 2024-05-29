package com.tonapps.wallet.data.pools

import android.content.Context
import com.tonapps.extensions.toByteArray
import com.tonapps.extensions.toParcel
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.pool.PoolEntity
import com.tonapps.wallet.api.entity.pool.PoolImplementationEntity
import com.tonapps.wallet.data.core.BlobDataSource
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.core.api.ApiDataRepository
import com.tonapps.wallet.data.core.api.Repository
import com.tonapps.wallet.data.pools.entities.StakingPoolsEntity
import com.tonapps.wallet.data.rates.RatesRepository
import io.tonapi.models.PoolImplementationType

class StakingPoolsRepository(
    context: Context,
    api: API,
    ratesRepository: RatesRepository
) {
    private val source = RemoteDataSource(context, api, ratesRepository)
    val storageFlow = source.storageFlow

    suspend fun doRequest(req: Request) = source.doRequest(req)

    suspend fun doRequest(accountId: String, testnet: Boolean) = doRequest(Request(accountId = accountId, testnet = testnet))

    /* * */

    data class Request(
        val accountId: String,
        //val language: String? = "en",
        val testnet: Boolean
    ) : com.tonapps.wallet.data.core.api.Request {
        override fun cacheKey(): String {
            val key = "staking_pools_${accountId}" //_${language}"

            if (!testnet) {
                return key
            }
            return "${key}_testnet"
        }
        override fun requestKey(): String = cacheKey()
    }

    data class Storage(
        private val source: RemoteDataSource
    ): Repository<Request, StakingPoolsEntity> {
        fun get( accountId: String, testnet: Boolean) = get(Request(accountId = accountId, testnet = testnet))

        override fun get(req: Request): ApiDataRepository.Result<StakingPoolsEntity>? = source.get(req)
    }

    class RemoteDataSource(
        context: Context,
        private val api: API,
        private val ratesRepository: RatesRepository
    ) : ApiDataRepository<Request, StakingPoolsEntity, Storage>() {
        private val localDataSource = LocalDataSource(context)

        override suspend fun requestImpl(request: Request): StakingPoolsEntity {
            val pools = api.staking(request.testnet).getStakingPools(
                availableFor = request.accountId,
                includeUnverified = false
            )

            val implEntitiesMap =
                pools.implementations.entries.associate {
                    it.key to PoolImplementationEntity(
                        PoolImplementationType.valueOf(it.key), it.value
                    )
                }

            val stakes = StakingPoolsEntity(
                poolsList = pools.pools.map { PoolEntity(it, implEntitiesMap[it.implementation.value]!!) }.sortedBy { it.address },
                implementationsList = implEntitiesMap.entries.map { it.value }.sortedBy { it.type.value },
            )

            if (!request.testnet) {
                ratesRepository.load(WalletCurrency.TON, stakes.liquidJettons.toMutableList())
            }

            return stakes
        }

        override fun storage(): Storage = Storage(this)

        override fun getCache(key: String): StakingPoolsEntity? {
            return localDataSource.getCache(key)
        }

        override fun clearCache(key: String) {
            localDataSource.clearCache(key)
        }

        override fun setCache(key: String, value: StakingPoolsEntity): Boolean {
            return localDataSource.updateCache(key, value)
        }
    }

    private class LocalDataSource(context: Context) : BlobDataSource<StakingPoolsEntity>(
        context = context,
        path = "staking_pools",
        lruInitialCapacity = 25
    ) {
        override fun onMarshall(data: StakingPoolsEntity) = data.toByteArray()

        override fun onUnmarshall(bytes: ByteArray) = bytes.toParcel<StakingPoolsEntity>()
    }
}