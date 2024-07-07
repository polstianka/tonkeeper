package com.tonapps.wallet.data.swap

import android.content.Context
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.AssetEntity
import com.tonapps.wallet.data.account.legacy.WalletManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WalletAssetsRepository(
    private val context: Context,
    private val api: API,
    private val walletManager: WalletManager,
) {

    private val localDataSource = WalletAssetsLocalDataSource(context)
    private val remoteDataSource = WalletAssetsRemoteDataSource(api)

    suspend fun get(): List<AssetEntity> {
        val wallet = walletManager.getWalletInfo() ?: return emptyList()
        val assets = localDataSource.getCache(wallet.accountId) ?: emptyList()
        if (assets.isNotEmpty()) {
            return assets
        }
        return load(wallet.accountId, wallet.address, wallet.testnet)
    }

    suspend fun getRemote(): List<AssetEntity> = withContext(Dispatchers.IO) {
        val wallet = walletManager.getWalletInfo() ?: return@withContext emptyList()
        load(wallet.accountId, wallet.address, wallet.testnet)
    }

    private suspend fun load(
        accountId: String,
        walletAddress: String,
        testnet: Boolean
    ): List<AssetEntity> = withContext(Dispatchers.IO) {
        val assets = remoteDataSource.load(walletAddress, testnet)
        localDataSource.setCache(accountId, assets)
        assets
    }
}