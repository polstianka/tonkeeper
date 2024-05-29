package com.tonapps.wallet.data.stonfi

import android.content.Context
import com.tonapps.wallet.data.stonfi.api.StonfiApi
import com.tonapps.wallet.data.stonfi.entities.StonfiAsset
import com.tonapps.wallet.data.stonfi.entities.StonfiAssetResponse
import com.tonapps.wallet.data.stonfi.entities.StonfiSimulate
import retrofit2.http.Query

class StonfiRepository(
    context: Context
) {
    private val stonfiApi: StonfiApi = StonfiApi.provideApi(context)
    private var cacheAssets: List<StonfiAsset>? = null
    private var cachedPairs: Map<String, List<String>>? = null

    suspend fun assets(
    ): List<StonfiAsset> {
        return cacheAssets ?: loadAssets()
    }

    suspend fun pairs(): Map<String, List<String>> {
        return cachedPairs ?: loadPairs()
    }

    private suspend fun loadPairs(): Map<String, List<String>> {
        val pairs = stonfiApi.pairs().getPairs()
        cachedPairs = pairs
        return pairs
    }

    private suspend fun loadAssets(): List<StonfiAsset> {
        val assets = stonfiApi.assets().assets.filter { it.defaultSymbol }
        cacheAssets = assets
        return assets
    }

    suspend fun simulate(offersAddress: String,
                         askAddress: String,
                         units: String,
                         slippageTolerance: String): StonfiSimulate {
        return stonfiApi.simulate(offersAddress, askAddress, units, slippageTolerance)
    }
}