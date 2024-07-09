package com.tonapps.tonkeeper.fragment.swap.domain

import com.tonapps.wallet.api.StonfiAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JettonWalletAddressRepository(
    private val stonfiAPI: StonfiAPI
) {

    private val cache = mutableMapOf<String, String>()
    suspend fun getJettonAddress(
        contractAddress: String,
        ownerAddress: String
    ): String = withContext(Dispatchers.IO) {
        val key = buildCacheKey(contractAddress, ownerAddress)
        val didHitCache = cache.containsKey(key)
        if (didHitCache) {
            cache[key]!!
        } else {
            val result = stonfiAPI.jetton.getWalletAddress(contractAddress, ownerAddress)
                .address
            cache[key] = result
            result
        }
    }

    private fun buildCacheKey(contractAddress: String, ownerAddress: String): String {
        return "$contractAddress$ownerAddress"
    }
}