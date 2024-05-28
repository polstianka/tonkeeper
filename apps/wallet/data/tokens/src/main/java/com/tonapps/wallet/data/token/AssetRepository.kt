package com.tonapps.wallet.data.token

import android.content.Context
import android.util.Log
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.token.entities.AssetEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class AssetRepository(
    private val context: Context,
    private val api: API
) {

    private val assetEntityCache = mutableMapOf<String, AssetEntity>()
    private var selectedSellTokenCache: String? = null
    private var selectedBuyTokenCache: String? = null

    // private val localDataSource = SwapLocalDataSource(context)
    // private val remoteDataSource = SwapRemoteDataSource(api)


    suspend fun get(
        testnet: Boolean
    ): Map<String, AssetEntity> = withContext(Dispatchers.IO) {

        val localAssetEntityMap = mutableMapOf<String, AssetEntity>()
        localAssetEntityMap.putAll(assetEntityCache)

        if (localAssetEntityMap.isNotEmpty()) {
            localAssetEntityMap
        } else {
            getRemote(testnet)
        }

    }

//    private suspend fun getLocal(
//        testnet: Boolean
//    ): Map<String, AssetEntity> = withContext(Dispatchers.IO) {
//        val assetEntityList = localDataSource.getCache(cacheKey())
//        val assetEntityMap =
//            assetEntityList?.associateBy { it.contractAddress }?.toMutableMap() ?: emptyMap()
//
//        Log.d("asset-get", "getLocal: ${assetEntityMap.toString()}")
//
//        assetEntityMap
//    }

    suspend fun getRemote(
        testnet: Boolean
    ): Map<String, AssetEntity> = withContext(Dispatchers.IO) {
        val assetEntityList = load(testnet)
        Log.d("asset-get", "getRemote: ${assetEntityList.toString()}")

        assetEntityList
    }

    private suspend fun load(
        testnet: Boolean
    ): Map<String, AssetEntity> = withContext(Dispatchers.IO) {

        val assetRequest = async {
            api.getAssets(false).map {
                AssetEntity(it)
            }
        }
        val pairRequest = async {
            api.getMarketPairs(false)
        }

        val assetList = assetRequest.await()
        val marketPairList = pairRequest.await()

        val assetMap: MutableMap<String, AssetEntity> =
            assetList.associateBy { it.contractAddress }.toMutableMap()

        val assetEntityMap: Map<String, AssetEntity> =
            if (assetList.isNotEmpty() && marketPairList.isNotEmpty()) {
                for (pair in marketPairList) {
                    if (assetMap.containsKey(pair[0]) && assetMap.containsKey(pair[1])) {
                        assetMap[pair[0]]?.swapableAssets?.add(pair[1])
                        assetMap[pair[1]]?.swapableAssets?.add(pair[0])
                    }
                }
                assetMap
            } else {
                emptyMap()
            }

        assetEntityCache.clear()
        assetEntityCache.putAll(assetEntityMap)

        /*var counter = 0
        assetEntityMap.values.toList().forEach {
            if(it.swapableAssets.isEmpty()) counter++
        }
        Log.d("asset-get", "assetList size: ${assetEntityMap.values.toList().size}")
        Log.d("asset-get", "empty counter: ${counter}")*/
        // Log.d("asset-get", "repo load: ${assetEntityMap}")

        assetEntityMap
    }

    fun setSelectedSellToken(contractAddress: String) {
        selectedSellTokenCache = contractAddress
    }

    fun getSelectedSellTokenCache() : AssetEntity? {
        // todo improve
        if(!selectedBuyTokenCache.isNullOrEmpty()) {
            return assetEntityCache[selectedBuyTokenCache]
        }
        return null
    }

}