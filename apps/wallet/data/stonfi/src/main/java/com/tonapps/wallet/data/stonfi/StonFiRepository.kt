package com.tonapps.wallet.data.stonfi

import android.content.Context
import com.tonapps.extensions.toByteArray
import com.tonapps.extensions.toParcel
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.core.BlobDataSource
import com.tonapps.wallet.data.core.api.ApiDataRepository
import com.tonapps.wallet.data.core.api.Repository
import com.tonapps.wallet.data.stonfi.entities.StonFiTokenEntity
import com.tonapps.wallet.data.stonfi.entities.StonFiTokensEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class StonFiRepository(
    context: Context,
    api: API
) {
    private val source = RemoteDataSource(context, api)
    val storageFlow = source.storageFlow

    suspend fun doRequest() {
        source.doRequest(Request)
    }

    object Request : com.tonapps.wallet.data.core.api.Request {
        override fun cacheKey(): String {
            return "stonfi_assets"
        }
        override fun requestKey(): String = cacheKey()
    }

    data class Storage(
        private val source: RemoteDataSource
    ): Repository<Request, StonFiTokensEntity> {
        override fun get(req: Request): ApiDataRepository.Result<StonFiTokensEntity>? = source.get(req)
    }

    class RemoteDataSource(
        context: Context,
        private val api: API,
    ) : ApiDataRepository<Request, StonFiTokensEntity, Storage>() {
        private val localDataSource = LocalDataSource(context)

        override suspend fun requestImpl(request: Request): StonFiTokensEntity =
            withContext(Dispatchers.IO) {
                val assetsDeferred = async { api.stonFiApi.dex.getAssetList() }
                val pairsDeferred = async { api.stonFiApi.dex.getMarketList() }

                val assets = assetsDeferred.await()
                val pairs = pairsDeferred.await()

                val list = assets.assetList.filter { !it.community }.sortedBy { it.contractAddress }

                val assetsMap = list.associate { it.contractAddress to TokenEntity(it) }
                val tokensMap = list.associate { it.contractAddress to mutableSetOf<TokenEntity>() }

                pairs.pairs.forEach {
                    assetsMap[it[0]]?.let { entity -> tokensMap[it[1]]?.add(entity) }
                    assetsMap[it[1]]?.let { entity -> tokensMap[it[0]]?.add(entity) }
                }

                StonFiTokensEntity(sort(list.mapNotNull {
                    val asset = assetsMap[it.contractAddress]
                    val tokens = tokensMap[it.contractAddress]
                    tokens?.let { t ->
                        asset?.let { a ->
                            StonFiTokenEntity(a, t.toList().sortedBy { ta -> ta.address })
                        }
                    }
                }))
            }

        override fun storage(): Storage = Storage(this)

        override fun getCache(key: String): StonFiTokensEntity? {
            return localDataSource.getCache(key)
        }

        override fun clearCache(key: String) {
            localDataSource.clearCache(key)
        }

        override fun setCache(key: String, value: StonFiTokensEntity): Boolean {
            return localDataSource.updateCache(key, value)
        }

        private fun sort(list: List<StonFiTokenEntity>): List<StonFiTokenEntity> {
            return list.sortedWith { first, second ->
                when {
                    first.token.isTon -> -1
                    second.token.isTon -> 1
                    first.token.isTetherUsdt -> -1
                    second.token.isTetherUsdt -> 1
                    first.token.isBridgedTetherUsdt -> -1
                    second.token.isBridgedTetherUsdt -> 1
                    else -> first.token.symbol.compareTo(second.token.symbol)
                }
            }
        }
    }

    private class LocalDataSource(context: Context) : BlobDataSource<StonFiTokensEntity>(
        context = context,
        path = "stonfi_assets",
        lruInitialCapacity = 25
    ) {
        override fun onMarshall(data: StonFiTokensEntity) = data.toByteArray()

        override fun onUnmarshall(bytes: ByteArray) = bytes.toParcel<StonFiTokensEntity>()
    }
}