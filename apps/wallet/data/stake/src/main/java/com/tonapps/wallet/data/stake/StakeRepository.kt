package com.tonapps.wallet.data.stake

import android.content.Context
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.StakePoolsEntity
import com.tonapps.wallet.data.account.legacy.WalletManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class StakeRepository(
    private val api: API,
    private val context: Context,
    private val walletManager: WalletManager,
) {

    private val localDataSource = StakeLocalDataSource(context)
    private val remoteDataSource = StakeRemoteDataSource(api)

    private val _selectedPoolAddress = MutableStateFlow("")
    val selectedPoolAddress: StateFlow<String> = _selectedPoolAddress

    fun select(address: String) {
        _selectedPoolAddress.value = address
    }

    suspend fun get(): StakePoolsEntity {
        val wallet = walletManager.getWalletInfo() ?: return StakePoolsEntity.Empty
        val pools = localDataSource.getCache(wallet.accountId) ?: StakePoolsEntity.Empty
        if (pools.pools.isNotEmpty()) {
            return pools
        }
        return load(wallet.accountId, wallet.address, wallet.testnet)
    }

    suspend fun getRemote(): StakePoolsEntity = withContext(Dispatchers.IO) {
        val wallet = walletManager.getWalletInfo() ?: return@withContext StakePoolsEntity.Empty
        load(wallet.accountId, wallet.address, wallet.testnet)
    }

    private suspend fun load(
        accountId: String,
        walletAddress: String,
        testnet: Boolean
    ): StakePoolsEntity = withContext(Dispatchers.IO) {
        val assets = remoteDataSource.load(walletAddress, testnet)
        localDataSource.setCache(accountId, assets)
        assets
    }
}