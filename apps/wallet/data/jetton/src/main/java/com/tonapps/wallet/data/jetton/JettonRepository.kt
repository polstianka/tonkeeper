package com.tonapps.wallet.data.jetton

import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.jetton.source.RemoteDataSource
import io.tonapi.models.StonfiJettonInfo

class JettonRepository(api: API) {

    private val remoteDataSource = RemoteDataSource(api)

    private var cachedJettons = emptyList<StonfiJettonInfo>()

    suspend fun get(
        loadCommunity: Boolean = false,
        testnet: Boolean
    ): List<StonfiJettonInfo> {
        if (cachedJettons.isNotEmpty()) {
            return cachedJettons
        }
        return remoteDataSource.load(loadCommunity = loadCommunity, testnet = testnet)
    }
}