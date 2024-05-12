package com.tonapps.wallet.data.swap

import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.AssetEntity
import io.ktor.network.sockets.SocketTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WalletAssetsRemoteDataSource(
    private val api: API
) {
    suspend fun load(walletAddress: String, testnet: Boolean): List<AssetEntity> =
        withContext(Dispatchers.IO) {
            try {
                api.getWalletAssets(walletAddress, testnet)
            } catch (e: SocketTimeoutException) {
                emptyList()
            }
        }
}