package com.tonapps.wallet.data.jetton.source

import com.tonapps.wallet.api.API
import io.tonapi.models.StonfiJettonInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class RemoteDataSource(private val api: API) {

    suspend fun load(
        loadCommunity: Boolean = false,
        testnet: Boolean,
    ): List<StonfiJettonInfo> = withContext(Dispatchers.IO) {
        api.getJettons(loadCommunity = loadCommunity, testnet = testnet)
    }

}