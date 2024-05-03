package com.tonapps.tonkeeper.ui.screen.swap

import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.AssetEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WalletAssetsRemoteDataSource(
    private val api: API
) {
    suspend fun load(walletAddress: String, testnet: Boolean): List<AssetEntity> =
        withContext(Dispatchers.IO) {
            api.getWalletAssets(walletAddress, testnet)
        }
}