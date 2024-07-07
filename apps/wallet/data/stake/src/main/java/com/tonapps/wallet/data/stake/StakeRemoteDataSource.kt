package com.tonapps.wallet.data.stake

import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.StakePoolsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StakeRemoteDataSource(
    private val api: API
) {
    suspend fun load(walletAddress: String, testnet: Boolean): StakePoolsEntity =
        withContext(Dispatchers.IO) {
            api.getStakingPools(walletAddress, testnet)
        }
}